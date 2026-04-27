package videoapp.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import videoapp.common.model.dto.InitUploadRequest;
import videoapp.common.model.jpa.UploadInfo;
import videoapp.common.model.presign.S3CompletedBatch;
import videoapp.common.model.presign.S3PresignedBatch;
import videoapp.common.model.presign.S3PresignedUrl;
import videoapp.common.utils.VideoUtils;
import videoapp.core.service.UploadInfoService;
import videoapp.core.service.s.VideoFileService;
import videoapp.storage.s3.S3PresignedUrlProvider;
import videoapp.storage.s3.repository.S3Repository;
import videoapp.web.config.WebProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static videoapp.common.Constants.ORIGINAL_VIDEO_FILE_TYPE;
import static videoapp.common.utils.VideoUtils.ceil;
import static videoapp.storage.s3.S3KeyResolver.buildTempVideoKey;

@Service
public class VideoService {

    private final VideoFileService videoFileService;
    private final UploadInfoService uploadInfoService;
    private final S3Repository s3Repository;
    private final S3PresignedUrlProvider s3PresignedUrlProvider;
    private final WebProperties webProperties;

    public VideoService(VideoFileService videoFileService,
                        UploadInfoService uploadInfoService,
                        S3Repository s3Repository,
                        S3PresignedUrlProvider s3PresignedUrlProvider,
                        WebProperties webProperties) {
        this.videoFileService = videoFileService;
        this.uploadInfoService = uploadInfoService;
        this.s3Repository = s3Repository;
        this.s3PresignedUrlProvider = s3PresignedUrlProvider;
        this.webProperties = webProperties;
    }


    @Transactional
    public S3PresignedBatch initiateMultipartUpload(InitUploadRequest initUploadRequest) {
        if (initUploadRequest.fileSizeBytes() <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }

        String publicId = VideoUtils.generateUniqueId();
        String key = buildTempVideoKey(publicId, initUploadRequest.fileName());
        String uploadId = s3Repository.createMultipartUpload(key).uploadId();
        int totalParts = ceil(initUploadRequest.fileSizeBytes(), webProperties.maxPartUploadSize());

        Map<Integer, PresignedUploadPartRequest> presignedUpload = s3PresignedUrlProvider.presignMultipartUpload(key, uploadId, totalParts);

        Instant expiresAt = presignedUpload.get(1).expiration();
        Instant creationTime = expiresAt.minus(webProperties.awsPresignedUrlLifetimeSec(), ChronoUnit.SECONDS);

        UploadInfo uploadInfo = enrichUploadInfo(publicId, key, uploadId, expiresAt, creationTime);

        videoFileService.initiateVideoFile(uploadInfo);

        List<S3PresignedUrl> presignedUrls = presignedUpload.entrySet().stream()
                .map(entry -> fillUrl(entry.getKey(), entry.getValue()))
                .toList();

        return new S3PresignedBatch(uploadId, key, presignedUrls, expiresAt);
    }

    @Transactional
    public ResponseEntity<Void> completeMultipartUpload(S3CompletedBatch completedBatch) {
        List<CompletedPart> completedParts = completedBatch.eTags().stream()
                .map(eTag -> CompletedPart.builder()
                        .partNumber(eTag.partNumber())
                        .eTag(eTag.eTag())
                        .build()
                )
                .toList();

        CompleteMultipartUploadResponse response = s3Repository.completeMultipartUpload(completedBatch.key(), completedBatch.uploadId(), completedParts);

        if (response.sdkHttpResponse().isSuccessful() && Objects.nonNull(response.eTag())) {
            uploadInfoService.updateUploadStatus(completedBatch.uploadId(), UploadStatus.PROCESSING);
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.badRequest().build();
    }

    @Transactional
    public ResponseEntity<Void> abortMultipartUpload(String key, String uploadId, String status) {
        try {
            AbortMultipartUploadResponse response = s3Repository.abortMultipartUpload(key, uploadId);

            if (!response.sdkHttpResponse().isSuccessful()) {
                return ResponseEntity.badRequest().build();
            }

            if (UploadStatus.CANCELLED.getStatus().equals(status) || UploadStatus.FAILED.getStatus().equals(status)) {
                uploadInfoService.updateUploadStatus(uploadId, UploadStatus.valueOf(status));
            }
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private UploadInfo enrichUploadInfo(String publicId, String key, String uploadId, long expiresAt, long creationTime) {
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.setS3OriginKey(key);
        uploadInfo.setUploadId(uploadId);
        uploadInfo.setCreatedAt(creationTime);


        return uploadInfo;
    }

    private S3PresignedUrl fillUrl(Integer partNumber, PresignedUploadPartRequest request) {
        Map<String, String> headers = request.httpRequest().headers().entrySet().stream()
                .collect(
                        Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().isEmpty() ? "" : e.getValue().get(0)
                        )
                );

        return new S3PresignedUrl(partNumber, request.url().toString(), headers);
    }
}
