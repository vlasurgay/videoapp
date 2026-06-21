package videoapp.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import videoapp.common.model.dto.InitUploadRequest;
import videoapp.common.model.dto.VideoConfigDto;
import videoapp.common.model.entity.Video;
import videoapp.common.model.enums.DubbingLanguage;
import videoapp.common.model.enums.VideoQualityProfile;
import videoapp.common.model.enums.VideoStatus;
import videoapp.common.model.upload.MultipartUploadContext;
import videoapp.common.model.upload.CompletedMultipartContext;
import videoapp.common.utils.Utils;
import videoapp.core.service.UploadInfoService;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;
import videoapp.storage.api.StorageProvider;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Service
public class UploadVideoService {

    private final VideoService videoService;
    private final UploadInfoService uploadInfoService;
    private final StorageProvider storageProvider;
    private final PathResolver pathResolver;

    public UploadVideoService(VideoService videoService, UploadInfoService uploadInfoService, StorageProvider storageProvider, PathResolver pathResolver) {
        this.videoService = videoService;
        this.uploadInfoService = uploadInfoService;
        this.storageProvider = storageProvider;
        this.pathResolver = pathResolver;
    }

    public VideoConfigDto getAvailableVideoConfig() {
        List<String> availableQualityProfiles = Arrays.stream(VideoQualityProfile.values())
                .map(profile -> Integer.toString(profile.getHeight()))
                .toList();
        return new VideoConfigDto(availableQualityProfiles, DubbingLanguage.availableCodes());
    }


    @Transactional
    public MultipartUploadContext initiateMultipartUpload(InitUploadRequest initUploadRequest) {
        if (initUploadRequest.fileSizeBytes() <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }
        String publicId = Utils.generateUniqueId();
        String key = pathResolver.buildTempFileKey(publicId, initUploadRequest.fileName());
        Instant createdAt = Instant.now();

        MultipartUploadContext uploadContext = storageProvider.createMultipartUpload(key, initUploadRequest.fileSizeBytes());

        Video video = videoService.initiateVideo(publicId, initUploadRequest, createdAt);
        uploadInfoService.initializeUploadInfo(video.getId(), uploadContext.uploadId(), key, uploadContext.expiresAt(), createdAt);

        log.info("multipart upload created, publicId={}", publicId);
        return uploadContext;
    }


    @Transactional
    public void completeMultipartUpload(CompletedMultipartContext completedMultipartContext) {
        storageProvider.completeMultipartUpload(
                completedMultipartContext.key(), completedMultipartContext.uploadId(), completedMultipartContext.uploadedParts()
        );
        videoService.updateVideoStatus(completedMultipartContext.uploadId(), VideoStatus.PROCESSING);

        log.debug("multipart upload completed, uploadId={}", completedMultipartContext.uploadId());
    }


    @Transactional
    public void abortMultipartUpload(String key, String uploadId, String status) {

        if (VideoStatus.ABORTED.equals(VideoStatus.valueOf(status)) || VideoStatus.FAILED.equals(VideoStatus.valueOf(status))) {
            storageProvider.abortMultipartUpload(key, uploadId);
            videoService.updateVideoStatus(uploadId, VideoStatus.valueOf(status));
            log.debug("multipart upload aborted with status={}, uploadId={}", status, uploadId);
        } else {
            log.warn("incorrect status to be aborted, provided status={}, uploadId={}", status, uploadId);

        }
    }
}
