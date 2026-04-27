package videoapp.common.model.track;

import lombok.Data;

@Data
public class SubtitleTrackMetadata extends TrackMetadata {
    private String language;
    private String codec;
}
