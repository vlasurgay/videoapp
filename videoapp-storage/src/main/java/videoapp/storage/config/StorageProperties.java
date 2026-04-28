package videoapp.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "videoapp.storage")
public record StorageProperties(
        String s3BucketName,
        long s3PresignedUrlLifetimeSec,
        long s3MaxPartUploadSize
) {
}