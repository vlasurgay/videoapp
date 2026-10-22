package videoapp.storage.s3.impl;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import videoapp.storage.api.ObjectStorage;
import videoapp.storage.api.StorageProvider;
import videoapp.storage.config.StorageProperties;

@Component
public class S3StorageProvider implements StorageProvider {

    private final ObjectStorage uploads;
    private final ObjectStorage processed;

    public S3StorageProvider(S3Client s3Client, StorageProperties properties, S3Presigner s3Presigner) {
        this.uploads = new S3BucketStorage(properties.s3UploadsBucket(), s3Client, properties, s3Presigner);
        this.processed = new S3BucketStorage(properties.s3ProcessedBucket(), s3Client, properties, s3Presigner);
    }

    @Override
    public ObjectStorage uploads() {
        return uploads;
    }

    @Override
    public ObjectStorage processed() {
        return processed;
    }
}
