package videoapp.worker.processing.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import videoapp.common.model.dto.Resolution;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.entity.Video;
import videoapp.common.utils.ProcessingWorkspace;
import videoapp.core.service.VideoService;
import videoapp.storage.s3.S3PresignedUrlProvider;
import videoapp.worker.config.WorkerProperties;
import videoapp.worker.ffmpeg.FfmpegCommandBuilder;
import videoapp.worker.ffmpeg.SegmentUploader;
import videoapp.worker.processing.JobProcessor;

import java.util.ArrayList;
import java.util.List;

import static videoapp.common.Constants.*;
import static videoapp.common.model.enums.JobType.TRANSCODE;
import static videoapp.storage.s3.S3KeyResolver.buildBaseHlsUploadKey;

@Component
public class TranscodeJobProcessor implements JobProcessor {

    private final VideoService videoService;
    private final S3PresignedUrlProvider presignedUrlProvider;
    private final SegmentUploader segmentUploader;
    private final WorkerProperties workerProperties;

    public TranscodeJobProcessor(VideoService videoService, S3PresignedUrlProvider presignedUrlProvider,
                                 SegmentUploader segmentUploader, WorkerProperties workerProperties) {
        this.videoService = videoService;
        this.presignedUrlProvider = presignedUrlProvider;
        this.segmentUploader = segmentUploader;
        this.workerProperties = workerProperties;
    }

    @Override
    public JobType getType() {
        return TRANSCODE;
    }

    @Override
    public void process(ProcessingJob job) {
        Video video = videoService.getById(job.getVideoId());

        try (ProcessingWorkspace workspace = new ProcessingWorkspace(video.getPublicId(), workerProperties.transcodeTempDir())) {
            String inputUrl = presignedUrlProvider.presignGetObject(video.getS3SourceKey()).url().toString();
            String baseS3Key = buildBaseHlsUploadKey(video.getPublicId());

            List<String> command = new FfmpegCommandBuilder()
                    .setOriginS3Key(inputUrl)
                    .setOutputDirectory(workspace.getAbsolutePath())
                    .setTargetQualityProfiles(parseResolution(job.getPayload()))
                    .build();

            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            segmentUploader.uploadFiles(workspace.getPath(), baseS3Key, process::isAlive);

            if (process.waitFor() != 0) {
                throw new RuntimeException("FFmpeg failed with exit code " + process.exitValue());
            }

            String masterKey = baseS3Key + "/master.m3u8";
            video.setMasterPlaylistKey(masterKey);
            videoService.save(video);


        } catch (Exception e) {
            throw new RuntimeException("FFmpeg transcode failed", e);
        }
    }

    private List<Resolution> parseResolution(JsonNode payload) {
        JsonNode targetResolutions = payload.get(TARGET_RESOLUTIONS);

        if (targetResolutions == null || !targetResolutions.isArray()) {
            throw new IllegalArgumentException("Invalid payload: targetResolutions missing");
        }

        List<Resolution> result = new ArrayList<>();
        for (JsonNode jsonResolution : targetResolutions) {
            result.add(
                    new Resolution(
                            jsonResolution.get(HEIGHT).asInt(),
                            jsonResolution.get(WIDTH).asInt(),
                            jsonResolution.get(BITRATE).asText()
                    )
            );
        }
        return result;
    }
}
