package videoapp.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "videoapp.storage")
public record StorageProperties(
        String cdnDomainName,
        String s3UploadsBucketName,
        String s3ProcessedBucketName,
        long s3PresignedUrlLifetimeSec,
        long s3MaxPartUploadSize
) {
}