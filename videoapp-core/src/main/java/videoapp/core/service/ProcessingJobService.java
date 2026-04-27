package videoapp.core.service;

import org.springframework.stereotype.Component;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.storage.jpa.repository.ProcessingJobRepository;

@Component
public class ProcessingJobService {

    private final ProcessingJobRepository jobRepository;

    public ProcessingJobService(ProcessingJobRepository jobRepository) {
        this.jobRepository = jobRepository;

    }

    public void saveAll(Iterable<ProcessingJob> jobs) {
        jobRepository.saveAll(jobs);
    }

    public void save(ProcessingJob job) {
        jobRepository.save(job);
    }
}
