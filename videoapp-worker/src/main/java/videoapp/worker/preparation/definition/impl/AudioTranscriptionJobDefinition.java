package videoapp.worker.preparation.definition.impl;

import org.springframework.stereotype.Component;
import videoapp.common.model.enums.JobType;
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
    public List<JobType> dependsOn() {
        return List.of(JobType.EXTRACT_AUDIO);
    }
}
