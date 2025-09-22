package vlsurhai.common.model.presign;

import com.fasterxml.jackson.annotation.JsonProperty;

public class S3PartETag {

    @JsonProperty("partNumber")
    private int partNumber;

    @JsonProperty("eTag")
    private String eTag;

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }
}
