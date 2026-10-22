package videoapp.storage.api;

import videoapp.common.model.upload.MultipartUploadContext;
import videoapp.common.model.upload.UploadedPart;

import java.nio.file.Path;
import java.util.Collection;

public interface ObjectStorage {
    MultipartUploadContext createMultipartUpload(String key, long fileSize);

    void completeMultipartUpload(String key, String uploadId, Collection<UploadedPart> uploadedParts);

    void abortMultipartUpload(String key, String uploadId);

    void moveObject(String sourceKey, String destinationKey, String destinationBucket);

    String getObjectPresignedUrl(String key);

    void putObject(String key, byte[] bytes, String contentType);

    void putObject(String key, Path filePath);

}
