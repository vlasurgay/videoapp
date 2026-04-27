package videoapp.common.model.job;

import com.fasterxml.jackson.databind.JsonNode;
import videoapp.common.model.dto.VideoMetadata;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.entity.Video;

public interface JobDefinition {
    JobType getType();
    JobType dependsOn();
    boolean isRequired(Video video, VideoMetadata metadata);
    JsonNode buildPayload(Video video, VideoMetadata metadata);
}
