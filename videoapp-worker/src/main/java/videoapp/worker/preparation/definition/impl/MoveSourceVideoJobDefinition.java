package videoapp.worker.preparation.definition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.worker.preparation.definition.JobDefinition;

import java.util.List;

import static videoapp.common.Constants.*;
import static videoapp.common.model.enums.JobType.MOVE_SOURCE_VIDEO;

@Component
public class MoveSourceVideoJobDefinition implements JobDefinition {

    @Override
    public JobType getType() {
        return MOVE_SOURCE_VIDEO;
    }

    @Override
    public List<JsonNode> buildPayloads(JobPlanningContext context) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();

        payload.put(ORIGIN_VIDEO_KEY, context.originKey());
        payload.put(PUBLIC_ID, context.publicId());

        return List.of(payload);

    }
}
