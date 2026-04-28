package videoapp.storage.s3.impl;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;
import videoapp.common.model.upload.MultipartPresignedUrl;
import videoapp.common.model.upload.MultipartUploadContext;
import videoapp.common.model.upload.UploadedPart;
import videoapp.storage.api.StorageProvider;
import videoapp.storage.config.StorageProperties;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static videoapp.common.utils.ContentTypeResolver.resolveContentType;
import static videoapp.common.utils.Utils.ceil;

@Repository
public class S3StorageProvider implements StorageProvider {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;


    public S3StorageProvider(S3Client s3Client, StorageProperties storageProperties, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.storageProperties = storageProperties;
        this.s3Presigner = s3Presigner;
    }


    @Override
    public void completeMultipartUpload(String key, String uploadId, Collection<UploadedPart> uploadedParts) {
        List<CompletedPart> awsParts = uploadedParts.stream()
                .map(p -> CompletedPart.builder()
                        .partNumber(p.partNumber())
                        .eTag(p.eTag())
                        .build()
                )
                .toList();

        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(storageProperties.s3BucketName())
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(awsParts).build())
                .build();

        s3Client.completeMultipartUpload(completeRequest);
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
        s3Client.abortMultipartUpload(b -> b
                .bucket(storageProperties.s3BucketName())
                .key(key)
                .uploadId(uploadId)
        );
    }

    @Override
    public void copyObject(String sourceKey, String destinationKey) {
        s3Client.copyObject(b -> b
                .sourceBucket(storageProperties.s3BucketName())
                .sourceKey(sourceKey)
                .destinationBucket(storageProperties.s3BucketName())
                .destinationKey(destinationKey)
        );
    }

    @Override
    public String getObjectPresignedUrl(String key) {
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(storageProperties.s3PresignedUrlLifetimeSec()))
                .getObjectRequest(builder -> builder
                        .bucket(storageProperties.s3BucketName())
                        .key(key)
                        .build())
                .build();


        return s3Presigner.presignGetObject(request).url().toString();
    }

    @Override
    public void putObject(String key, byte[] bytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(storageProperties.s3BucketName())
                .contentType(contentType)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    @Override
    public void putObject(String key, Path filePath) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(storageProperties.s3BucketName())
                .key(key)
                .contentType(resolveContentType(filePath))
                .build();

        s3Client.putObject(request, RequestBody.fromFile(filePath));
    }

    @Override
    public MultipartUploadContext createMultipartUpload(String key, long fileSize) {
        CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
                .bucket(storageProperties.s3BucketName())
                .key(key)
                .build();

        CreateMultipartUploadResponse multipartUploadResponse = s3Client.createMultipartUpload(initRequest);

        int totalParts = ceil(fileSize, storageProperties.s3MaxPartUploadSize());
        List<MultipartPresignedUrl> urls = IntStream.rangeClosed(1, totalParts)
                .mapToObj(partNum -> presignUploadPart(key, multipartUploadResponse.uploadId(), partNum))
                .toList();

        Instant expiresAt = Instant.now().plusSeconds(storageProperties.s3PresignedUrlLifetimeSec());

        return new MultipartUploadContext(
                multipartUploadResponse.uploadId(), key, storageProperties.s3MaxPartUploadSize(), urls, expiresAt
        );
    }

    private MultipartPresignedUrl presignUploadPart(String key, String uploadId, int partNumber) {
        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(storageProperties.s3PresignedUrlLifetimeSec()))
                .uploadPartRequest(b -> b
                        .bucket(storageProperties.s3BucketName())
                        .key(key)
                        .partNumber(partNumber)
                        .uploadId(uploadId))
                .build();

        PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(presignRequest);

        Map<String, String> headers = presigned.httpRequest().headers().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

        return new MultipartPresignedUrl(partNumber, presigned.url().toString(), headers);
    }
}
