package videoapp.worker.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.entity.Video;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.enums.TrackType;
import videoapp.common.model.processing.ProcessingWorkspace;
import videoapp.common.model.processing.UploadStats;
import videoapp.common.model.track.AudioTrackMetadata;
import videoapp.common.model.track.TrackMetadata;
import videoapp.core.service.MediaTrackService;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;
import videoapp.storage.api.StorageProvider;
import videoapp.worker.config.WorkerProperties;
import videoapp.worker.integration.ffmpeg.FfmpegCommandBuilder;
import videoapp.worker.processor.JobProcessor;
import videoapp.worker.service.UploadSegmentService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static videoapp.common.Constants.ORIGINAL_AUDIO_FOR_DUB;
import static videoapp.common.Constants.TARGET_LANGUAGE;
import static videoapp.common.model.enums.JobType.AI_DUBBING;
import static videoapp.common.utils.JsonNodeExtractor.extractString;

@Slf4j
@Component
public class AudioDubbingJobProcessor implements JobProcessor {

    private final VideoService videoService;
    private final MediaTrackService mediaTrackService;
    private final UploadSegmentService uploadSegmentService;
    private final StorageProvider storageProvider;
    private final PathResolver pathResolver;
    private final WorkerProperties workerProperties;

    public AudioDubbingJobProcessor(VideoService videoService, MediaTrackService mediaTrackService,
                                    UploadSegmentService uploadSegmentService, StorageProvider storageProvider,
                                    PathResolver pathResolver, WorkerProperties workerProperties) {
        this.videoService = videoService;
        this.mediaTrackService = mediaTrackService;
        this.uploadSegmentService = uploadSegmentService;
        this.storageProvider = storageProvider;
        this.pathResolver = pathResolver;
        this.workerProperties = workerProperties;
    }

    @Override
    public JobType getType() {
        return AI_DUBBING;
    }

    @Override
    public void process(ProcessingJob job) {
        Video video = videoService.getById(job.getVideoId());
        String targetLanguage = extractString(job.getPayload(), TARGET_LANGUAGE);
        String baseUploadKey = pathResolver.buildBaseHlsDirKey(video.getPublicId());

        try (ProcessingWorkspace workspace = new ProcessingWorkspace(workerProperties.temporalOutputDirectory())) {
            Path originalAudioPath = downloadOriginalAudio(video, workspace);
            Path dubbedWavPath = workspace.getPath().resolve("dubbed_" + targetLanguage + ".wav");

            runLocalPythonDubbing(originalAudioPath, targetLanguage, dubbedWavPath, workspace.getAbsolutePath());

            Process process = runAudioHls(dubbedWavPath.toAbsolutePath().toString(), targetLanguage, workspace);
            Map<String, UploadStats> uploadStats = uploadSegmentService.uploadFiles(workspace.getOutputDir(), baseUploadKey, process::isAlive);

            if (process.waitFor() != 0) {
                throw new RuntimeException("FFmpeg HLS segmentation failed for dubbing with exit code " + process.exitValue());
            }

            initMediaTrack(video.getId(), targetLanguage, uploadStats);

        } catch (IOException ioException) {
            log.error("IO Exception during dubbing process: ", ioException);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path downloadOriginalAudio(Video video, ProcessingWorkspace workspace) throws IOException {
        Path originalAudioPath = workspace.getPath().resolve(ORIGINAL_AUDIO_FOR_DUB);
        String presignedUrl = storageProvider.getObjectPresignedUrl(video.getSourceAudioKey());

        try (InputStream in = new URL(presignedUrl).openStream()) {
            Files.copy(in, originalAudioPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return originalAudioPath;
    }

    private void runLocalPythonDubbing(Path inputAudio, String lang, Path outputAudio, String workDir) throws Exception {
        List<String> command = List.of(
                "python3",
                "dubbing_tool.py",
                "--input", inputAudio.toAbsolutePath().toString(),
                "--lang", lang,
                "--output", outputAudio.toAbsolutePath().toString(),
                "--workdir", workDir
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        processBuilder.directory(new java.io.File("/app"));

        Process aiProcess = processBuilder.start();

        String scriptOutput = new String(aiProcess.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

        int exitCode = aiProcess.waitFor();
        if (exitCode != 0) {
            log.error("Dubbing script error\n{}", scriptOutput);
            throw new RuntimeException("AI Dubbing failed with exit code " + exitCode);
        } else {
            log.info("Dubbing completed successfully for language: {}", lang);
        }
    }

    private Process runAudioHls(String sourceFilePath, String language, ProcessingWorkspace workspace) throws Exception {
        Path languageOutputDir = workspace.getOutputDir().resolve(language);
        Files.createDirectories(languageOutputDir);

        List<String> commands = new FfmpegCommandBuilder()
                .setMode(FfmpegCommandBuilder.Mode.HLS_AUDIO)
                .setSourceUrl(sourceFilePath)
                .setOutputFilesDirectory(languageOutputDir.toAbsolutePath().toString())
                .build();

        return new ProcessBuilder(commands).redirectErrorStream(true).start();
    }

    private void initMediaTrack(Long videoId, String language, Map<String, UploadStats> uploadStats) {
        UploadStats stats = uploadStats.values().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No dubbed audio file uploaded"));
        TrackMetadata metadata = AudioTrackMetadata.from(language, stats.getTotalBytes(), true);
        mediaTrackService.initializeMediaTrack(videoId, TrackType.AUDIO, language, stats.getFileUploadKey(), metadata);
    }
}