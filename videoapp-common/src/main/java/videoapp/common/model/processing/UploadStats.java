package videoapp.common.model.processing;

import lombok.Data;

@Data
public class QualityStats {
    private long totalBytes;
    private String playlistS3Key;

    public void addBytes(long size) {
        this.totalBytes += size;
    }
}
