package videoapp.storage.s3.repository.impl;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;
import videoapp.common.model.presign.MultipartUploadContext;
import videoapp.common.model.presign.S3PresignedUrl;
import videoapp.common.model.presign.UploadedPart;
import videoapp.storage.StorageProvider;
import videoapp.storage.s3.config.S3Properties;

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
    private final S3Properties s3Properties;


    public S3StorageProvider(S3Client s3Client, S3Properties s3Properties, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
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
                .bucket(s3Properties.bucketName())
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(awsParts).build())
                .build();

        s3Client.completeMultipartUpload(completeRequest);
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
        s3Client.abortMultipartUpload(b -> b
                .bucket(s3Properties.bucketName())
                .key(key)
                .uploadId(uploadId)
        );
    }

    @Override
    public void copyObject(String sourceKey, String destinationKey) {
        s3Client.copyObject(b -> b
                .sourceBucket(s3Properties.bucketName())
                .sourceKey(sourceKey)
                .destinationBucket(s3Properties.bucketName())
                .destinationKey(destinationKey)
        );
    }

    @Override
    public String getObjectPresignedUrl(String key) {
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.presignedUrlLifetimeSec()))
                .getObjectRequest(builder -> builder
                        .bucket(s3Properties.bucketName())
                        .key(key)
                        .build())
                .build();


        return s3Presigner.presignGetObject(request).url().toString();
    }

    @Override
    public void putObject(String key, byte[] bytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .contentType(contentType)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    @Override
    public void putObject(String key, Path filePath) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(key)
                .contentType(resolveContentType(filePath))
                .build();

        s3Client.putObject(request, RequestBody.fromFile(filePath));
    }

    @Override
    public MultipartUploadContext createMultipartUpload(String key, long fileSize) {
        CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
                .bucket(s3Properties.bucketName())
                .key(key)
                .build();

        CreateMultipartUploadResponse multipartUploadResponse = s3Client.createMultipartUpload(initRequest);

        int totalParts = ceil(fileSize, s3Properties.maxPartUploadSize());
        List<S3PresignedUrl> urls = IntStream.rangeClosed(1, totalParts)
                .mapToObj(partNum -> presignUploadPart(key, multipartUploadResponse.uploadId(), partNum))
                .toList();

        Instant expiresAt = Instant.now().plusSeconds(s3Properties.presignedUrlLifetimeSec());

        return new MultipartUploadContext(
                multipartUploadResponse.uploadId(), key, s3Properties.maxPartUploadSize(), urls, expiresAt
        );
    }

    private S3PresignedUrl presignUploadPart(String key, String uploadId, int partNumber) {
        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Properties.presignedUrlLifetimeSec()))
                .uploadPartRequest(b -> b
                        .bucket(s3Properties.bucketName())
                        .key(key)
                        .partNumber(partNumber)
                        .uploadId(uploadId))
                .build();

        PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(presignRequest);

        Map<String, String> headers = presigned.httpRequest().headers().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

        return new S3PresignedUrl(partNumber, presigned.url().toString(), headers);
    }

















//    @Override
//    public void putObject(String key, byte[] bytes, String contentType) {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(s3Properties.bucketName())
//                .contentType(contentType)
//                .key(key)
//                .build();
//
//        s3Client.putObject(request, RequestBody.fromBytes(bytes));
//    }
//
//    public void putObject(String key, Path filePath) {
//        PutObjectRequest request = PutObjectRequest.builder()
//                .bucket(s3Properties.bucketName())
//                .key(key)
//                .contentType(resolveContentType(filePath))
//                .build();
//
//        s3Client.putObject(request, RequestBody.fromFile(filePath));
//    }
//
//    @Override
//    public CreateMultipartUploadResponse createMultipartUpload(String key) {
//        CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
//                .bucket(s3Properties.bucketName())
//                .key(key)
//                .build();
//
//        return s3Client.createMultipartUpload(initRequest);
//    }
//
//    @Override
//    public CompleteMultipartUploadResponse completeMultipartUpload(String key, String uploadId, Collection<CompletedPart> parts) {
//        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
//                .bucket(s3Properties.bucketName())
//                .key(key)
//                .uploadId(uploadId)
//                .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build())
//                .build();
//
//        return s3Client.completeMultipartUpload(completeRequest);
//    }
//
//    @Override
//    public AbortMultipartUploadResponse abortMultipartUpload(String key, String uploadId) {
//        AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
//                .bucket(s3Properties.bucketName())
//                .key(key)
//                .uploadId(uploadId)
//                .build();
//
//        return s3Client.abortMultipartUpload(abortRequest);
//    }
//
//    @Override
//    public CopyObjectResponse copyObject(String sourceKey, String destinationKey) {
//        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
//                .sourceBucket(s3Properties.bucketName())
//                .sourceKey(sourceKey)
//                .destinationBucket(s3Properties.bucketName())
//                .destinationKey(destinationKey)
//                .build();
//
//        return s3Client.copyObject(copyRequest);
//    }
}
