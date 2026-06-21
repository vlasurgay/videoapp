package videoapp.worker.preparation.definition;

import com.fasterxml.jackson.databind.JsonNode;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.JobPlanningContext;

import java.util.Collections;
import java.util.List;

public interface JobDefinition {

    JobType getType();
    default List<JsonNode> buildPayloads(JobPlanningContext context) {
        return Collections.singletonList(null);
    }

    default boolean isRequired(JobPlanningContext context) {
        return true;
    }

    default List<JobType> dependsOn() {
        return Collections.emptyList();
    };
}
