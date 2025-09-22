package vlsurhai.storage.s3;

import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

import java.util.Collection;

public interface S3Repository {
    void putObject(String key, byte[] content);

    CreateMultipartUploadResponse createMultipartUpload(String key);

    CompleteMultipartUploadResponse completeMultipartUpload(String key, String uploadId, Collection<CompletedPart> parts);

    AbortMultipartUploadResponse abortMultipartUpload(String key, String uploadId);

    }
