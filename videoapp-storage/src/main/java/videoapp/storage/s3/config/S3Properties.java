package videoapp.storage.s3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
        String bucketName,
        long presignedUrlLifetimeSec,
        long maxPartUploadSize
) {
}