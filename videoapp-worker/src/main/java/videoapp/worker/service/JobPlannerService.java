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
import java.util.Objects;

@Service
public class JobPlannerService {

    private final List<JobDefinition> jobDefinitions;
    private final ProcessingJobService processingJobService;

    public JobPlannerService(List<JobDefinition> jobDefinitions, ProcessingJobService processingJobService) {
        this.jobDefinitions = jobDefinitions;
        this.processingJobService = processingJobService;
    }

    public void planJobs(JobPlanningContext context) {
        Map<JobType, List<ProcessingJob>> createdJobs = new HashMap<>();

        for (JobDefinition definition : jobDefinitions) {
            if (definition.isRequired(context)) {
                List<ProcessingJob> jobs = definition.buildPayloads(context).stream()
                        .map(payload -> createJob(context, definition.getType(), payload))
                        .toList();

                createdJobs.put(definition.getType(), jobs);
            }
        }

        for (JobDefinition definition : jobDefinitions) {
            List<ProcessingJob> currentJobs = createdJobs.get(definition.getType());
            if (currentJobs == null || currentJobs.isEmpty()) {
                continue;
            }

            List<ProcessingJob> listedJobs = definition.dependsOn().stream()
                    .map(createdJobs::get)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();

            currentJobs.forEach(job -> job.getDependencies().addAll(listedJobs));
        }
        processingJobService.saveAll(
                createdJobs.values().stream()
                        .flatMap(List::stream)
                        .toList()
        );
    }

    private ProcessingJob createJob(JobPlanningContext context, JobType type, com.fasterxml.jackson.databind.JsonNode payload) {
        ProcessingJob processingJob = new ProcessingJob();

        processingJob.setVideoId(context.videoId());
        processingJob.setType(type);
        processingJob.setPayload(payload);

        return processingJob;
    }
}
