package vlsurhai.storage.s3.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import vlsurhai.storage.s3.S3Repository;

import java.util.Collection;

@Repository
@ConditionalOnProperty(value = "is.s3.mode.enabled", havingValue = "true", matchIfMissing = true)
public class S3RepositoryImpl implements S3Repository {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Override
    public void putObject(String key, byte[] content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));
    }

    @Override
    public CreateMultipartUploadResponse createMultipartUpload(String key) {
        CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.createMultipartUpload(initRequest);
    }

    @Override
    public CompleteMultipartUploadResponse completeMultipartUpload(String key, String uploadId, Collection<CompletedPart> parts) {
        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build())
                .build();

        return s3Client.completeMultipartUpload(completeRequest);
    }

    @Override
    public AbortMultipartUploadResponse abortMultipartUpload(String key, String uploadId) {
        AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .build();

        return s3Client.abortMultipartUpload(abortRequest);
    }
}
