package vlsurhai.storage.s3.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import vlsurhai.storage.s3.S3Repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

@Repository
@ConditionalOnProperty(value = "is.s3.mode.enabled", havingValue = "false")
public class S3RepositoryStub implements S3Repository {

    private static final String LOCAL_STORAGE_PATH = "./local-s3";

    @Deprecated
    @Override
    public void putObject(String key, byte[] content) {
        try {
            File targetFile = new File(LOCAL_STORAGE_PATH, key);

            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                Files.createDirectories(parentDir.toPath());
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(content);
            }

            System.out.println("Saved locally: " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file locally", e);
        }
    }

    @Override
    public CreateMultipartUploadResponse createMultipartUpload(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompleteMultipartUploadResponse completeMultipartUpload(String key, String uploadId, Collection<CompletedPart> parts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbortMultipartUploadResponse abortMultipartUpload(String key, String uploadId) {
        throw new UnsupportedOperationException();
    }

}
