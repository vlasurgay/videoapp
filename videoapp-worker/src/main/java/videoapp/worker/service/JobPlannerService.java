package videoapp.worker.service;

import org.springframework.stereotype.Service;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.core.service.ProcessingJobService;
import videoapp.worker.preparation.definition.JobDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobPlannerService {

    private final List<JobDefinition> jobDefinitions;
    private final ProcessingJobService processingJobService;

    public JobPlannerService(List<JobDefinition> jobDefinitions, ProcessingJobService processingJobService) {
        this.jobDefinitions = jobDefinitions;
        this.processingJobService = processingJobService;
    }

    public void planJobs(JobPlanningContext context) {
        Map<JobType, ProcessingJob> createdJobs = new HashMap<>();

        for (JobDefinition jobDefinition : jobDefinitions) {
            if (jobDefinition.isRequired(context)) {
                ProcessingJob processingJob = new ProcessingJob();

                processingJob.setVideoId(context.videoId());
                processingJob.setType(jobDefinition.getType());
                processingJob.setPayload(jobDefinition.buildPayload(context));

                createdJobs.put(jobDefinition.getType(), processingJob);
            }
        }

        for (JobDefinition definition : jobDefinitions) {
            ProcessingJob currentJob = createdJobs.get(definition.getType());

            if (currentJob != null && definition.dependsOn() != null) {
                definition.dependsOn().stream()
                        .map(createdJobs::get)
                        .forEach(currentJob.getDependencies()::add);
            }
        }
        processingJobService.saveAll(createdJobs.values());
    }
}
