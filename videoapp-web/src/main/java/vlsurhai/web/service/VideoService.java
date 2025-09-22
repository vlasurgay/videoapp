package vlsurhai.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import vlsurhai.common.model.presign.S3CompletedBatch;
import vlsurhai.common.model.presign.S3PresignedBatch;
import vlsurhai.common.model.presign.S3PresignedUrl;
import vlsurhai.common.model.video.UploadInfo;
import vlsurhai.common.model.video.UploadStatus;
import vlsurhai.common.model.video.VideoFile;
import vlsurhai.common.utils.VideoUtils;
import vlsurhai.storage.jpa.UploadRepository;
import vlsurhai.storage.s3.S3PresignService;
import vlsurhai.storage.s3.S3Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static vlsurhai.common.Constants.BASIC_UPLOAD_S3_KEY;

@Service
public class VideoService {

    @Autowired
    private UploadRepository uploadRepository;

    @Autowired
    private S3Repository s3Repository;

    @Autowired
    private S3PresignService s3PresignService;

    @Value("${aws.s3.presigned-url-lifetime-min:180000}")
    private long presignedUrlLifeTimeMin;


    @Transactional
    public S3PresignedBatch initiateMultipartUpload(UploadInfo uploadInfo) {

        if (uploadInfo.getTotalParts() <= 0) {
            throw new IllegalArgumentException("Total parts must be positive");
        }

        String publicId = VideoUtils.generateUniqueId();
        String key = String.format(BASIC_UPLOAD_S3_KEY, publicId, uploadInfo.getFileName());
        String uploadId = s3Repository.createMultipartUpload(key).uploadId();

        Map<Integer, PresignedUploadPartRequest> presignedUpload = s3PresignService.presignMultipartUpload(key, uploadId, uploadInfo.getTotalParts());
        long expiresAt = presignedUpload.get(1).expiration().toEpochMilli();

        uploadInfo.setPublicId(publicId);
        uploadInfo.getOriginalMetadata().setPublicId(publicId);
        uploadInfo.setCreationTime(expiresAt - presignedUrlLifeTimeMin);
        uploadInfo.setStatus(UploadStatus.INITIATED);
        uploadInfo.getOriginalMetadata().setS3Key(key);
        uploadInfo.setUploadId(uploadId);
        uploadInfo.setExpiresAt(expiresAt);
        uploadInfo.setVideoFile(initiateVideoFile(uploadInfo));

        uploadRepository.save(uploadInfo);

        S3PresignedBatch batch = new S3PresignedBatch();

        batch.setKey(key);
        batch.setUploadId(uploadId);
        batch.setExpiresAt(expiresAt);

        batch.setPresignedUrls(
                presignedUpload.entrySet().stream()
                        .map(entry -> fillUrl(entry.getKey(), entry.getValue()))
                        .toList()
        );

        return batch;
    }

    @Transactional
    public ResponseEntity<Void> completeMultipartUpload(S3CompletedBatch completedBatch) {
        List<CompletedPart> completedParts = completedBatch.getETags().stream()
                .map(eTag -> CompletedPart.builder()
                        .partNumber(eTag.getPartNumber())
                        .eTag(eTag.getETag())
                        .build()
                )
                .toList();

        CompleteMultipartUploadResponse response = s3Repository.completeMultipartUpload(completedBatch.getKey(), completedBatch.getUploadId(), completedParts);

        if (response.sdkHttpResponse().isSuccessful() && Objects.nonNull(response.eTag())) {
            uploadRepository.updateStatusByUploadId(completedBatch.getUploadId(), UploadStatus.PROCESSING);
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

            if (UploadStatus.CANCELLED.getStatus().equals(status)) {
                uploadRepository.updateStatusByUploadId(uploadId, UploadStatus.CANCELLED);
            } else if (UploadStatus.FAILED.getStatus().equals(status)) {
                uploadRepository.updateStatusByUploadId(uploadId, UploadStatus.FAILED);
            }

            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private VideoFile initiateVideoFile(UploadInfo uploadInfo) {

        VideoFile videoFile = new VideoFile();

        videoFile.setUploadInfo(uploadInfo);
        videoFile.setCreationTime(uploadInfo.getCreationTime());
        videoFile.setPublicId(uploadInfo.getPublicId());
        videoFile.setTitle(uploadInfo.getTitle());
        videoFile.setDescription(uploadInfo.getDescription());
        videoFile.setOriginalMetadata(uploadInfo.getOriginalMetadata());

        return videoFile;
    }

    private S3PresignedUrl fillUrl(Integer partNumber, PresignedUploadPartRequest presignedRequest) {

        S3PresignedUrl url = new S3PresignedUrl();
        url.setUrl(presignedRequest.url().toString());
        url.setPartNumber(partNumber);

        url.setHeaders(new HashMap<>());
        presignedRequest.httpRequest().headers().forEach((k, v) -> url.getHeaders().put(k, v.isEmpty() ? "" : v.get(0)));

        return url;
    }
}
