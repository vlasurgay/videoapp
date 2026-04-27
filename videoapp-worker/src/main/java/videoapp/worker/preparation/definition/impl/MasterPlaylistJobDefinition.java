package videoapp.worker.preparation.definition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.worker.preparation.definition.JobDefinition;

import java.util.List;

import static videoapp.common.Constants.PUBLIC_ID;
import static videoapp.common.model.enums.JobType.GENERATE_MASTER_PLAYLIST;
import static videoapp.common.model.enums.JobType.TRANSCODE;

@Component
public class MasterPlaylistJobDefinition implements JobDefinition {
    @Override
    public JobType getType() {
        return GENERATE_MASTER_PLAYLIST;
    }

    @Override
    public List<JobType> dependsOn() {
        return List.of(TRANSCODE);
    }

    @Override
    public JsonNode buildPayload(JobPlanningContext context) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();

        payload.put(PUBLIC_ID, context.publicId());

        return payload;
    }
}