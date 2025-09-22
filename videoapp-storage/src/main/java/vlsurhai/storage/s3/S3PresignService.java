package vlsurhai.storage.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class S3PresignService {

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-lifetime-min:180000}")
    private long presignedUrlLifeTimeMin;

    public Map<Integer, PresignedUploadPartRequest> presignMultipartUpload(String key, String uploadId, int totalParts) {

        return IntStream
                .rangeClosed(1, totalParts)
                .parallel()
                .boxed()
                .collect(Collectors.toMap(
                        partNumber -> partNumber,
                        partNumber -> presignUploadPart(key, uploadId, partNumber),
                        (firstValue, secondValue) -> firstValue,
                        LinkedHashMap::new
                ));
    }

    public PresignedUploadPartRequest presignUploadPart(String key, String uploadId, int partNumber) {
        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofMillis(presignedUrlLifeTimeMin))
                .uploadPartRequest(builder -> builder
                        .bucket(bucketName)
                        .key(key)
                        .partNumber(partNumber)
                        .uploadId(uploadId)
                        .build())
                .build();

        return s3Presigner.presignUploadPart(presignRequest);
    }
}
