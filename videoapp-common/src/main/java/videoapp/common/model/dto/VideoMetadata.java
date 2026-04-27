package videoapp.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {
    private String format;
    private Double duration;
    private Integer height;
    private Integer width;
    private Long bitrate;
    private Boolean hasAudio;
    private Long fileSizeBytes;
}
