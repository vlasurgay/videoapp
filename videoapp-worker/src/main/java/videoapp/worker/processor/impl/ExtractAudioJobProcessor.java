package videoapp.worker.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.entity.Video;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.ProcessingWorkspace;
import videoapp.common.model.processing.UploadStats;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;
import videoapp.storage.api.StorageProvider;
import videoapp.worker.config.WorkerProperties;
import videoapp.worker.integration.ffmpeg.FfmpegCommandBuilder;
import videoapp.worker.processor.JobProcessor;
import videoapp.worker.service.UploadSegmentService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static videoapp.common.Constants.FILE_NAME;
import static videoapp.common.Constants.ORIGIN_VIDEO_KEY;
import static videoapp.common.model.enums.JobType.EXTRACT_AUDIO;
import static videoapp.common.utils.JsonNodeExtractor.extractString;

@Slf4j
@Component
public class ExtractAudioJobProcessor implements JobProcessor {

    private final VideoService videoService;
    private final StorageProvider storageProvider;
    private final PathResolver pathResolver;
    private final UploadSegmentService uploadSegmentService;
    private final WorkerProperties workerProperties;

    public ExtractAudioJobProcessor(VideoService videoService, StorageProvider storageProvider, PathResolver pathResolver,
                                    UploadSegmentService uploadSegmentService, WorkerProperties workerProperties) {
        this.videoService = videoService;
        this.storageProvider = storageProvider;
        this.pathResolver = pathResolver;
        this.uploadSegmentService = uploadSegmentService;
        this.workerProperties = workerProperties;
    }

    @Override
    public JobType getType() {
        return EXTRACT_AUDIO;
    }

    @Override
    public void process(ProcessingJob job) {
        Video video = videoService.getById(job.getVideoId());

        String sourceUrl = extractString(job.getPayload(), ORIGIN_VIDEO_KEY);
        String fileName = extractString(job.getPayload(), FILE_NAME);

        String uploadDir = pathResolver.buildSourceDirKey(video.getPublicId());
        String presignedUrl = storageProvider.getObjectPresignedUrl(sourceUrl);

        try (ProcessingWorkspace workspace = new ProcessingWorkspace(workerProperties.temporalOutputDirectory())) {
            Process process = runExtractAudio(presignedUrl, workspace, fileName);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg extract audio failed with exit code " + exitCode);
            }

            Map<String, UploadStats> uploadStats = uploadSegmentService.uploadFiles(workspace.getOutputDir(), uploadDir, process::isAlive);

            UploadStats uploadedFileStat = retrieveSourceAudioKey(uploadStats);
            videoService.updateAudioSourceKey(video.getPublicId(), uploadedFileStat.getFileUploadKey());

            log.debug("ExtractAudioJobProcessor succeed, publicId={}", video.getPublicId());

        } catch (IOException e) {
            throw new RuntimeException("FFmpeg extract audio failed", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Process runExtractAudio(String sourceUrl, ProcessingWorkspace workspace, String fileName) throws Exception {
        List<String> command = new FfmpegCommandBuilder()
                .setSourceUrl(sourceUrl)
                .setOutputFilesDirectory(workspace.getOutputAbsolutePath())
                .setOutputAudioFileName(fileName)
                .setMode(FfmpegCommandBuilder.Mode.EXTRACT_AUDIO)
                .build();

        return new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
    }

    private UploadStats retrieveSourceAudioKey(Map<String, UploadStats> statsMap) {
        return statsMap.values().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No audio file uploaded"));
    }
}
