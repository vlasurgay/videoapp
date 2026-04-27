package videoapp.worker.processing;

import videoapp.common.model.enums.JobType;
import videoapp.common.model.entity.ProcessingJob;

public interface JobProcessor {

    JobType getType();

    void process(ProcessingJob job);
}
