package videoapp.worker.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.enums.JobType;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;
import videoapp.storage.api.StorageProvider;
import videoapp.storage.config.StorageProperties;
import videoapp.worker.processor.JobProcessor;

import static videoapp.common.Constants.ORIGIN_VIDEO_KEY;
import static videoapp.common.Constants.PUBLIC_ID;
import static videoapp.common.model.enums.JobType.PROMOTE_SOURCE_VIDEO;
import static videoapp.common.utils.JsonNodeExtractor.extractString;
import static videoapp.common.utils.UploadKeyUtils.extractFileNameFromUploadKey;

@Slf4j
@Component
public class PromoteSourceVideoJobProcessor implements JobProcessor {

    private final VideoService videoService;
    private final StorageProvider storageProvider;
    private final PathResolver pathResolver;
    private final StorageProperties storageProperties;


    public PromoteSourceVideoJobProcessor(VideoService videoService, StorageProvider storageProvider,
                                          PathResolver pathResolver, StorageProperties storageProperties) {
        this.videoService = videoService;
        this.storageProvider = storageProvider;
        this.pathResolver = pathResolver;
        this.storageProperties = storageProperties;
    }

    @Override
    public JobType getType() {
        return PROMOTE_SOURCE_VIDEO;
    }

    @Override
    public void process(ProcessingJob job) {
        String publicId = extractString(job.getPayload(), PUBLIC_ID);
        String originKey = extractString(job.getPayload(), ORIGIN_VIDEO_KEY);

        String fileName = extractFileNameFromUploadKey(originKey);
        String sourceFileKey = pathResolver.buildSourceFileKey(publicId, fileName);

        storageProvider.uploads().moveObject(originKey, storageProperties.s3ProcessedBucket(), sourceFileKey);

        videoService.updateVideoSourceKey(publicId, sourceFileKey);

        log.debug("PromoteSourceVideoJobProcessor succeed, publicId={}", publicId);
    }
}
