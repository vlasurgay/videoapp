package videoapp.worker.preparation.definition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.worker.preparation.definition.JobDefinition;

import java.util.List;

import static videoapp.common.Constants.ORIGIN_VIDEO_KEY;
import static videoapp.common.Constants.FILE_NAME;
import static videoapp.common.model.enums.JobType.EXTRACT_AUDIO;

@Component
public class ExtractAudioJobDefinition implements JobDefinition {

    @Override
    public JobType getType() {
        return EXTRACT_AUDIO;
    }

    @Override
    public boolean isRequired(JobPlanningContext context) {
        return context.targetSettings() != null && !context.targetSettings().getMuted();
    }

    @Override
    public List<JsonNode> buildPayloads(JobPlanningContext context) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();

        payload.put(ORIGIN_VIDEO_KEY, context.originKey());
        payload.put(FILE_NAME, context.fileName());

        return List.of(payload);
    }
}
