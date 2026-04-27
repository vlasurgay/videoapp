package videoapp.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.entity.Video;
import videoapp.common.model.processing.VideoProcessingContext;
import videoapp.core.service.VideoService;
import videoapp.storage.s3.S3KeyResolver;
import videoapp.storage.s3.repository.S3Repository;
import videoapp.worker.service.JobPlannerService;
import videoapp.worker.service.ProbeVideoService;

@Slf4j
@Component
public class VideoPreparationManager {

    private final VideoService videoService;
    private final S3Repository s3Repository;
    private final ProbeVideoService probeVideoService;
    private final JobPlannerService jobPlannerService;

    public VideoPreparationManager(VideoService videoService, S3Repository s3Repository,
                                   ProbeVideoService probeVideoService, JobPlannerService jobPlannerService) {
        this.videoService = videoService;
        this.s3Repository = s3Repository;
        this.probeVideoService = probeVideoService;
        this.jobPlannerService = jobPlannerService;
    }

    public void process(VideoProcessingContext context) {
        Video video = videoService.findByPublicId(context.getPublicId());

        copyToSourceDirectory(context);
        probeVideoService.probe(video.getId(), context);
        jobPlannerService.planJobs(video, context.getVideoMetadata());

        log.info("Video {} is ready for processing. Jobs dispatched.", video.getPublicId());
    }

    private void copyToSourceDirectory(VideoProcessingContext context) {
        String currentS3Key = context.getOriginS3Key();
        String newS3SourceKey = S3KeyResolver.buildSourceVideoKey(context.getPublicId(), context.getFileName());

        s3Repository.copyObject(currentS3Key, newS3SourceKey);
        context.setS3SourceKey(newS3SourceKey);

        videoService.updateSourceKey(context.getPublicId(), newS3SourceKey);

        log.info("Successfully moved origin file to source directory: {}", newS3SourceKey);
    }
}
