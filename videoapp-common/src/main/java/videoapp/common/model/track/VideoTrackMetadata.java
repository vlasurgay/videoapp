package videoapp.common.model.track;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import videoapp.common.model.dto.Resolution;
import videoapp.common.model.processing.UploadStats;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoTrackMetadata extends TrackMetadata {
    private Integer width;
    private Integer height;
    private Long bitrate;
    private Double duration;

    public VideoTrackMetadata(Integer height, Integer width) {
        this.height = height;
        this.width = width;
    }

    public static VideoTrackMetadata from(Resolution res, UploadStats stats) {
        VideoTrackMetadata meta = new VideoTrackMetadata();

        meta.setWidth(res.width());
        meta.setHeight(res.height());
        meta.setBitrate(res.bitrate());
        meta.setFileSizeBytes(stats.getTotalBytes());
        meta.setFormat("hls/h264");

        return meta;
    }
}
