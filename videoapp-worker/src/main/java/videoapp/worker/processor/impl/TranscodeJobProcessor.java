package videoapp.worker.processor.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import videoapp.common.model.dto.Resolution;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.entity.Video;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.enums.TrackType;
import videoapp.common.model.processing.ProcessingWorkspace;
import videoapp.common.model.processing.UploadStats;
import videoapp.common.model.track.TrackMetadata;
import videoapp.common.model.track.VideoTrackMetadata;
import videoapp.core.service.MediaTrackService;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;
import videoapp.storage.api.StorageProvider;
import videoapp.worker.config.WorkerProperties;
import videoapp.worker.integration.ffmpeg.FfmpegCommandBuilder;
import videoapp.worker.processor.JobProcessor;
import videoapp.worker.service.UploadSegmentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static videoapp.common.Constants.*;
import static videoapp.common.model.enums.JobType.TRANSCODE;
import static videoapp.worker.integration.ffmpeg.FfmpegCommandBuilder.Mode.HLS_VIDEO;

@Component
public class TranscodeJobProcessor implements JobProcessor {

    private final VideoService videoService;
    private final MediaTrackService mediaTrackService;
    private final StorageProvider storageProvider;
    private final PathResolver pathResolver;
    private final UploadSegmentService uploadSegmentService;
    private final WorkerProperties workerProperties;

    public TranscodeJobProcessor(VideoService videoService, MediaTrackService mediaTrackService, StorageProvider storageProvider, PathResolver pathResolver,
                                 UploadSegmentService uploadSegmentService, WorkerProperties workerProperties) {
        this.videoService = videoService;
        this.mediaTrackService = mediaTrackService;
        this.storageProvider = storageProvider;
        this.pathResolver = pathResolver;
        this.uploadSegmentService = uploadSegmentService;
        this.workerProperties = workerProperties;
    }

    @Override
    public JobType getType() {
        return TRANSCODE;
    }

    @Override
    public void process(ProcessingJob job) {
        Video video = videoService.getById(job.getVideoId());
        List<Resolution> resolutions = parseResolution(job.getPayload());

        String baseUploadKey = pathResolver.buildBaseHlsDirKey(video.getPublicId());
        String presignedUrl = storageProvider.getObjectPresignedUrl(video.getSourceVideoKey());

        try (ProcessingWorkspace workspace = new ProcessingWorkspace(workerProperties.temporalOutputDirectory())) {

            Process process = runTranscode(presignedUrl, workspace, resolutions);
            Map<String, UploadStats> uploadStats = uploadSegmentService.uploadFiles(workspace.getPath(), baseUploadKey, process::isAlive);

            if (process.waitFor() != 0) {
                throw new RuntimeException("FFmpeg failed with exit code " + process.exitValue());
            }

            initMediaTracks(video.getId(), resolutions, uploadStats);

        } catch (Exception e) {
            throw new RuntimeException("FFmpeg transcode failed", e);
        }
    }

    private Process runTranscode(String sourceUrl, ProcessingWorkspace workspace, List<Resolution> resolutions) throws Exception {
        List<String> command = new FfmpegCommandBuilder()
                .setMode(HLS_VIDEO)
                .setSourceUrl(sourceUrl)
                .setOutputFilesDirectory(workspace.getAbsolutePath())
                .setTargetQualityProfiles(resolutions)
                .build();

        return new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
    }

    private void initMediaTracks(Long videoId, List<Resolution> resolutions, Map<String, UploadStats> uploadStats) {
        resolutions.forEach(res -> {
            UploadStats stats = uploadStats.get(res.label());
            if (stats != null) {
                TrackMetadata metadata = VideoTrackMetadata.from(res, stats);
                mediaTrackService.initializeMediaTrack(videoId, TrackType.VIDEO, res.label(), stats.getFileUploadKey(), metadata);
            }
        });
    }

    private List<Resolution> parseResolution(JsonNode payload) {
        JsonNode targetResolutions = payload.get(TARGET_RESOLUTIONS);

        if (targetResolutions == null || !targetResolutions.isArray()) {
            throw new IllegalArgumentException("Missing target resolutions in ProcessingJob");
        }

        List<Resolution> result = new ArrayList<>();
        for (JsonNode json : targetResolutions) {
            result.add(
                    new Resolution(json.get(HEIGHT).asInt(), json.get(WIDTH).asInt(), json.get(BITRATE).asLong(), json.get(LABEL).asText())
            );
        }
        return result;
    }
}
