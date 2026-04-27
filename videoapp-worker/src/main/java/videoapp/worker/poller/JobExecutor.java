package videoapp.worker.poller;

import org.springframework.stereotype.Component;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.worker.processor.JobProcessor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JobDispatcher {

    private final Map<JobType, JobProcessor> processors;

    public JobDispatcher(List<JobProcessor> processorList) {
        this.processors = processorList.stream()
                .collect(Collectors.toMap(JobProcessor::getType, jobProcessor -> jobProcessor));
    }

    public void dispatch(ProcessingJob job) {
        JobType type = job.getType();
        JobProcessor processor = processors.get(type);

        if (processor == null) {
            throw new IllegalStateException("No processor found for job type: " + type);
        }
        processor.process(job);
    }
}
