package videoapp.worker.preparation.definition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import videoapp.common.model.enums.DubbingLanguage;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.worker.preparation.definition.JobDefinition;

import java.util.List;

import static videoapp.common.Constants.TARGET_LANGUAGE;
import static videoapp.common.model.enums.JobType.AI_DUBBING;
import static videoapp.common.model.enums.JobType.EXTRACT_AUDIO;

@Slf4j
@Component
public class AudioDubbingJobDefinition implements JobDefinition {

    @Override
    public JobType getType() {
        return AI_DUBBING;
    }

    @Override
    public List<JobType> dependsOn() {
        return List.of(EXTRACT_AUDIO);
    }

    @Override
    public boolean isRequired(JobPlanningContext context) {
        return context.targetSettings() != null
                && !CollectionUtils.isEmpty(context.targetSettings().getTargetLanguages());
    }

    @Override
    public List<JsonNode> buildPayloads(JobPlanningContext context) {
        return resolveLanguages(context).stream()
                .map(this::buildPayloadForLanguage)
                .toList();
    }

    private List<String> resolveLanguages(JobPlanningContext context) {
        if (context.targetSettings() == null || CollectionUtils.isEmpty(context.targetSettings().getTargetLanguages())) {
            return List.of();
        }

        return context.targetSettings().getTargetLanguages().stream()
                .distinct()
                .filter(lang -> {
                    boolean supported = DubbingLanguage.isSupported(lang);
                    if (!supported) {
                        log.warn("Dub language '{}' is not supported", DubbingLanguage.getDisplayNameByCode(lang));
                    }
                    return supported;
                })
                .toList();
    }

    private JsonNode buildPayloadForLanguage(String language) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put(TARGET_LANGUAGE, language);
        return payload;
    }
}
