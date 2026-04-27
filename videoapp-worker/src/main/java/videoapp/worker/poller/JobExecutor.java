package videoapp.worker.poller;

import org.springframework.stereotype.Component;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.enums.JobType;
import videoapp.core.service.ProcessingJobService;
import videoapp.worker.config.WorkerProperties;
import videoapp.worker.processor.JobProcessor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JobExecutor {

    private final ProcessingJobService jobService;
    private final WorkerProperties workerProperties;
    private final Map<JobType, JobProcessor> processors;

    public JobExecutor(ProcessingJobService jobService, WorkerProperties workerProperties, List<JobProcessor> processorList) {
        this.jobService = jobService;
        this.workerProperties = workerProperties;
        this.processors = processorList.stream()
                .collect(Collectors.toMap(JobProcessor::getType, jobProcessor -> jobProcessor));
    }

    public void execute(ProcessingJob job) {
        JobType type = job.getType();
        JobProcessor processor = processors.get(type);

        if (processor == null) {
            throw new IllegalStateException("No processor found for job type: " + type);
        }
        try {
            processor.process(job);
            job.complete();

        } catch (Exception e) {
            job.fail(e.getMessage(), workerProperties.jobNextRetryDelaySec());
        }
        jobService.save(job);
    }
}
