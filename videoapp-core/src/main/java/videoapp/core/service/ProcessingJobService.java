package videoapp.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.storage.jpa.repository.ProcessingJobRepository;

@Slf4j
@Service
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
