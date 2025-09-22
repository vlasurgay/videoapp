package vlsurhai.common.model.presign;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class S3CompletedBatch {

    @JsonProperty("key")
    private String key;

    @JsonProperty("uploadId")
    private String uploadId;

    @JsonProperty("eTags")
    List<S3PartETag> eTags;


    public List<S3PartETag> getETags() {
        return eTags;
    }

    public void setETags(List<S3PartETag> eTags) {
        this.eTags = eTags;
    }

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
}
