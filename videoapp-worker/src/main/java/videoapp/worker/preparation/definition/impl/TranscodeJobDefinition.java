package videoapp.worker.preparation.definition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import videoapp.common.model.dto.TargetSettings;
import videoapp.common.model.dto.VideoMetadata;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.enums.VideoQualityProfile;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.worker.preparation.definition.JobDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static videoapp.common.Constants.*;
import static videoapp.common.model.enums.JobType.MOVE_SOURCE_VIDEO;
import static videoapp.common.model.enums.JobType.TRANSCODE;

@Component
public class TranscodeJobDefinition implements JobDefinition {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JobType getType() {
        return TRANSCODE;
    }

    @Override
    public List<JobType> dependsOn() {
        return List.of(MOVE_SOURCE_VIDEO);
    }

    @Override
    public JsonNode buildPayload(JobPlanningContext context) {
        List<VideoQualityProfile> profiles = resolveProfiles(context.targetSettings(), context.videoMetadata());

        ObjectNode payload = objectMapper.createObjectNode();

        ArrayNode resolutions = payload.putArray(TARGET_RESOLUTIONS);
        addResolutions(resolutions, profiles, context.videoMetadata());

        return payload;
    }

    private List<VideoQualityProfile> resolveProfiles(TargetSettings targetSettings, VideoMetadata metadata) {
        if (targetSettings == null || CollectionUtils.isEmpty(targetSettings.getTargetResolutions())) {
            return Collections.emptyList();
        }

        return targetSettings.getTargetResolutions().stream()
                .map(this::findProfileByString)
                .flatMap(Optional::stream)
                .filter(p -> p.getHeight() < metadata.getHeight())
                .distinct()
                .toList();
    }

    private Optional<VideoQualityProfile> findProfileByString(String res) {
        return Arrays.stream(VideoQualityProfile.values())
                .filter(p -> res.contains(String.valueOf(p.getHeight())))
                .findFirst();
    }

    private void addResolutions(ArrayNode array, List<VideoQualityProfile> profiles, VideoMetadata metadata) {
        for (VideoQualityProfile p : profiles) {
            addResolutionNode(
                    array,
                    p.getLabel(),
                    p.getHeight(),
                    defineWidth(p, metadata),
                    Math.multiplyExact(Long.parseLong(p.getBitrateKbps()), 1000L)
            );
        }
        addResolutionNode(
                array,
                String.format(QUALITY_LABEL, metadata.getHeight()),
                metadata.getHeight(),
                metadata.getWidth(),
                metadata.getBitrate()
        );
    }

    private void addResolutionNode(ArrayNode array, String label, int height, int width, long bitrate) {
        array.addObject()
                .put(LABEL, label)
                .put(HEIGHT, height)
                .put(WIDTH, width)
                .put(BITRATE, bitrate);
    }

    private int defineWidth(VideoQualityProfile profile, VideoMetadata metadata) {
        double aspectRatio = (double) metadata.getWidth() / metadata.getHeight();
        int targetWidth = (int) Math.round(profile.getHeight() * aspectRatio);

        if (targetWidth % 2 != 0) {
            targetWidth++;
        }

        return targetWidth;
    }
}
