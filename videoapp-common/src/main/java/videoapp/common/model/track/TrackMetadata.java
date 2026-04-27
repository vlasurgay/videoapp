package videoapp.common.model.track;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = VideoTrackMetadata.class, name = "VIDEO"),
        @JsonSubTypes.Type(value = AudioTrackMetadata.class, name = "AUDIO"),
        @JsonSubTypes.Type(value = SubtitleTrackMetadata.class, name = "SUBTITLE")
})
public abstract class TrackMetadata {
    private String format;
    private long fileSizeBytes;
}
