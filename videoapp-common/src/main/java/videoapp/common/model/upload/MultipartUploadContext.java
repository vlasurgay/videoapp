package videoapp.common.model.presign;

import java.time.Instant;
import java.util.List;

public record MultipartUploadContext(
        String uploadId,
        String key,
        Long maxPartUploadSize,
        List<MultipartPresignedUrl> presignedUrls,
        Instant expiresAt
) {
}
