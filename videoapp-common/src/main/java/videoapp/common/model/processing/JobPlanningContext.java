package videoapp.common.model.processing;

import videoapp.common.model.dto.TargetSettings;
import videoapp.common.model.dto.VideoMetadata;

public record JobPlanningContext(
        Long videoId,
        String publicId,
        String fileName,
        String originKey,
        VideoMetadata videoMetadata,
        TargetSettings targetSettings

) {
}
