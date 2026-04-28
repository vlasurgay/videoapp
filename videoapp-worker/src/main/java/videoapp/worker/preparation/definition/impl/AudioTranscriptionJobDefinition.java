package videoapp.worker.preparation.definition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.worker.preparation.definition.JobDefinition;

import java.util.List;

import static videoapp.common.model.enums.JobType.AUDIO_TRANSCRIPTION;

@Component
public class AudioTranscriptionJobDefinition implements JobDefinition {


    @Override
    public JobType getType() {
        return AUDIO_TRANSCRIPTION;
    }

    @Override
    public JsonNode buildPayload(JobPlanningContext context) {
        return null;
    }

    @Override
    public List<JobType> dependsOn() {
        return List.of(JobType.EXTRACT_AUDIO);
    }
}
