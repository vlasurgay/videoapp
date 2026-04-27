package videoapp.worker.poller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import videoapp.storage.jpa.repository.ProcessingJobRepository;

@Component
public class JobPoller {

    private final ProcessingJobRepository jobRepository;
    private final JobExecutor jobExecutor;

    public JobPoller(ProcessingJobRepository jobRepository, JobExecutor jobExecutor) {
        this.jobRepository = jobRepository;
        this.jobExecutor = jobExecutor;
    }

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        jobRepository.pickNextJob().ifPresent(jobExecutor::execute);
    }
}
