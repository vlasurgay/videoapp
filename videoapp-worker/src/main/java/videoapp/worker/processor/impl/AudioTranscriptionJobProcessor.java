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
import videoapp.worker.utils.ProcessExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static videoapp.common.model.enums.JobType.AUDIO_TRANSCRIPTION;

@Slf4j
@Component
public class AudioTranscriptionJobProcessor implements JobProcessor {

    private static final Pattern SILENCE_END_PATTERN = Pattern.compile("silence_end:\\s*(\\d+(?:\\.\\d+)?)");
    private static final double SAMPLE_DURATION_SEC = 30.0;

    private final VideoService videoService;
    private final MediaTrackService mediaTrackService;
    private final StorageProvider storageProvider;
    private final ProcessExecutor processExecutor;
    private final UploadSegmentService uploadSegmentService;
    private final PathResolver pathResolver;
    private final WorkerProperties workerProperties;

    public AudioTranscriptionJobProcessor(VideoService videoService, MediaTrackService mediaTrackService, StorageProvider storageProvider,
                                          ProcessExecutor processExecutor, UploadSegmentService uploadSegmentService,
                                          PathResolver pathResolver, WorkerProperties workerProperties) {
        this.videoService = videoService;
        this.mediaTrackService = mediaTrackService;
        this.storageProvider = storageProvider;
        this.processExecutor = processExecutor;
        this.uploadSegmentService = uploadSegmentService;
        this.pathResolver = pathResolver;
        this.workerProperties = workerProperties;
    }

    @Override
    public JobType getType() {
        return AUDIO_TRANSCRIPTION;
    }

    @Override
    public void process(ProcessingJob job) {
        Video video = videoService.getById(job.getVideoId());
        String presignedUrl = storageProvider.getObjectPresignedUrl(video.getSourceAudioKey());

        try (ProcessingWorkspace workspace = new ProcessingWorkspace(workerProperties.temporalOutputDirectory())) {
            double silenceEndTime = findSilenceEndTime(presignedUrl);
            String language = resolveLanguage(presignedUrl, silenceEndTime);
            String baseUploadKey = pathResolver.buildBaseHlsDirKey(video.getPublicId());

            Process process = runAudioHls(presignedUrl, language, workspace);
            Map<String, UploadStats> uploadStats = uploadSegmentService.uploadFiles(workspace.getOutputDir(), baseUploadKey, process::isAlive);

            if (process.waitFor() != 0) {
                throw new RuntimeException("FFmpeg failed with exit code " + process.exitValue());
            }

            initMediaTrack(video.getId(), language, uploadStats);

            log.debug("AudioTranscriptionJobProcessor succeed, publicId={}", video.getPublicId());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("FFmpeg audio transcription failed", e);
        }
    }

    private double findSilenceEndTime(String sourceKey) {
        List<String> commands = new FfmpegCommandBuilder()
                .setSourceUrl(sourceKey)
                .setMode(FfmpegCommandBuilder.Mode.FIND_SILENCE_END)
                .setLogLevel(FfmpegCommandBuilder.LogLevel.INFO)
                .build();

        String output = processExecutor.executeWithOutput(commands);

        Matcher matcher = SILENCE_END_PATTERN.matcher(output);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0.0;
    }

    private String resolveLanguage(String url, double startSec) {
        List<String> ffmpegCmd = new FfmpegCommandBuilder()
                .setSourceUrl(url)
                .setSourceFileStartTimeCodeSec(startSec)
                .setSourceFileDurationSec(SAMPLE_DURATION_SEC)
                .setMode(FfmpegCommandBuilder.Mode.EXTRACT_AUDIO_FRAGMENT)
                .setLogLevel(FfmpegCommandBuilder.LogLevel.QUIET)
                .build();

        List<String> whisperCmd = List.of("python3", "transcribe.py");

        String output = processExecutor.executePipedProcessesWithOutput(ffmpegCmd, whisperCmd);

        return parseLanguage(output);
    }

    private Process runAudioHls(String sourceKey, String language, ProcessingWorkspace workspace) throws IOException {
        Path languageOutputDir = workspace.getOutputDir().resolve(language);
        Files.createDirectories(languageOutputDir);

        List<String> commands = new FfmpegCommandBuilder()
                .setMode(FfmpegCommandBuilder.Mode.HLS_AUDIO)
                .setSourceUrl(sourceKey)
                .setOutputFilesDirectory(languageOutputDir.toAbsolutePath().toString())
                .build();

        return new ProcessBuilder(commands)
                .redirectErrorStream(true)
                .start();
    }

    private void initMediaTrack(Long videoId, String language, Map<String, UploadStats> uploadStats) {
        UploadStats stats = uploadStats.values().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No audio file uploaded"));

        TrackMetadata metadata = AudioTrackMetadata.from(language, stats.getTotalBytes(), true);
        mediaTrackService.initializeMediaTrack(videoId, TrackType.AUDIO, language, stats.getFileUploadKey(), metadata);
    }

    private String parseLanguage(String output) {
        return output.trim();
    }
}
