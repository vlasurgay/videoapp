package videoapp.common.model.processing;

import lombok.Data;

@Data
public class UploadStats {
    private long totalBytes;
    private String fileUploadKey;

    public void addBytes(long size) {
        this.totalBytes += size;
    }
}
