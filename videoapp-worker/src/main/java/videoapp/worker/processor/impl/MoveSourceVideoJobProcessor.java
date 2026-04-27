package videoapp.worker.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.enums.JobType;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;
import videoapp.storage.api.StorageProvider;
import videoapp.worker.processor.JobProcessor;

import static videoapp.common.Constants.ORIGIN_VIDEO_KEY;
import static videoapp.common.Constants.PUBLIC_ID;
import static videoapp.common.model.enums.JobType.MOVE_SOURCE_VIDEO;
import static videoapp.common.utils.JsonNodeExtractor.extractString;
import static videoapp.common.utils.UploadKeyUtils.extractFileNameFromUploadKey;

@Slf4j
@Component
public class MoveSourceVideoJobProcessor implements JobProcessor {

    private final VideoService videoService;
    private final StorageProvider storageProvider;
    private final PathResolver pathResolver;


    public MoveSourceVideoJobProcessor(VideoService videoService, StorageProvider storageProvider, PathResolver pathResolver) {
        this.videoService = videoService;
        this.storageProvider = storageProvider;
        this.pathResolver = pathResolver;
    }

    @Override
    public JobType getType() {
        return MOVE_SOURCE_VIDEO;
    }

    @Override
    public void process(ProcessingJob job) {
        String publicId = extractString(job.getPayload(), PUBLIC_ID);
        String originKey = extractString(job.getPayload(), ORIGIN_VIDEO_KEY);

        String fileName = extractFileNameFromUploadKey(originKey);
        String sourceFileKey = pathResolver.buildSourceFileKey(publicId, fileName);

        storageProvider.copyObject(originKey, sourceFileKey);

        videoService.updateVideoSourceKey(publicId, sourceFileKey);

        log.debug("MoveSourceVideoJobProcessor succeed, publicId={}", publicId);
    }
}
