package videoapp.worker;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.storage.jpa.repository.ProcessingJobRepository;

import static videoapp.common.model.enums.JobStatus.COMPLETED;
import static videoapp.common.model.enums.JobStatus.FAILED;

@Component
public class JobPoller {

    private final ProcessingJobRepository jobRepository;
    private final JobDispatcher jobDispatcher;

    public JobPoller(ProcessingJobRepository jobRepository, JobDispatcher jobDispatcher) {
        this.jobRepository = jobRepository;
        this.jobDispatcher = jobDispatcher;
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        jobRepository.pickNextJob().ifPresent(this::execute);
    }

    private void execute(ProcessingJob job) {
        try {
            jobDispatcher.dispatch(job);
            jobRepository.updateStatusById(job.getId(), COMPLETED);
        } catch (Exception e) {
            jobRepository.updateStatusById(job.getId(), FAILED);
        }
    }
}
