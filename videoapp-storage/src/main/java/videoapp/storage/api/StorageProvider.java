package videoapp.storage;

import videoapp.common.model.presign.MultipartUploadContext;
import videoapp.common.model.presign.UploadedPart;

import java.nio.file.Path;
import java.util.Collection;

public interface StorageProvider {

    MultipartUploadContext createMultipartUpload(String key, long fileSize);

    void completeMultipartUpload(String key, String uploadId, Collection<UploadedPart> uploadedParts);

    void abortMultipartUpload(String key, String uploadId);

    void copyObject(String sourceKey, String destinationKey);

    String getObjectPresignedUrl(String key);

    void putObject(String key, byte[] bytes, String contentType);

    void putObject(String key, Path filePath);






//    void putObject(String key, byte[] content, String contentType);
//
//    void putObject(String key, Path filePath);
//
//    CreateMultipartUploadResponse createMultipartUpload(String key);
//
//    CompleteMultipartUploadResponse completeMultipartUpload(String key, String uploadId, Collection<CompletedPart> parts);
//
//    AbortMultipartUploadResponse abortMultipartUpload(String key, String uploadId);
//
//    CopyObjectResponse copyObject(String sourceKey, String destinationKey);

    }
