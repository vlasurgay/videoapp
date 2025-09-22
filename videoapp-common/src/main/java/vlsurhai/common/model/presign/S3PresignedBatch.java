package vlsurhai.common.model.presign;

import java.util.List;

public class S3PresignedBatch {

    private String uploadId;
    private String key;
    private List<S3PresignedUrl> presignedUrls;
    private Long expiresAt;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<S3PresignedUrl> getPresignedUrls() {
        return presignedUrls;
    }

    public void setPresignedUrls(List<S3PresignedUrl> presignedUrls) {
        this.presignedUrls = presignedUrls;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
