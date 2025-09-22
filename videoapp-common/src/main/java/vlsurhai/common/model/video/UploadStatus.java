package vlsurhai.common.model.video;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UploadStatus {
    INITIATED("initiated"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String status;
    UploadStatus(String status) { this.status = status; }

    @JsonValue
    public String getStatus() {
        return status;
    }
}
