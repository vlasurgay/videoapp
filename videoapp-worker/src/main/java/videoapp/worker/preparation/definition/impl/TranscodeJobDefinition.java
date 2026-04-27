package videoapp.worker.preparation.definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import videoapp.common.model.dto.VideoMetadata;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.dto.TargetSettings;
import videoapp.common.model.entity.Video;
import videoapp.common.model.enums.VideoQualityProfile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static videoapp.common.Constants.*;
import static videoapp.common.model.enums.JobType.TRANSCODE;

@Component
public class TranscodeJobDefinition implements JobDefinition {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JobType getType() {
        return TRANSCODE;
    }

    @Override
    public JsonNode buildPayload(Video video, VideoMetadata metadata) {
        List<VideoQualityProfile> profiles = resolveProfiles(video, metadata);
        return toPayload(profiles, metadata);
    }

    private List<VideoQualityProfile> resolveProfiles(Video video, VideoMetadata metadata) {
        TargetSettings settings = video.getTargetSettings();

        if (settings == null || settings.getTargetResolutions() == null) {
            return Collections.emptyList();
        }

        return settings.getTargetResolutions().stream()
                .map(this::findProfileByString)
                .flatMap(Optional::stream)
                .filter(p -> p.getHeight() < metadata.getHeight())
                .distinct()
                .toList();
    }

    private ObjectNode toPayload(List<VideoQualityProfile> profiles, VideoMetadata metadata) {
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode resolutions = payload.putArray(TARGET_RESOLUTIONS);

        for (VideoQualityProfile p : profiles) {
            addResolutionNode(
                    resolutions,
                    p.getLabel(),
                    p.getHeight(),
                    p.getWidth(),
                    Math.multiplyExact(Long.parseLong(p.getBitrateKbps()), 1000L)
            );
        }

        addResolutionNode(resolutions,
                String.format(QUALITY_LABEL, metadata.getHeight()),
                metadata.getHeight(),
                metadata.getWidth(),
                metadata.getBitrate()
        );

        return payload;
    }

    private void addResolutionNode(ArrayNode array, String label, int height, int width, long bitrate) {
        array.addObject()
                .put(LABEL, label)
                .put(HEIGHT, height)
                .put(WIDTH, width)
                .put(BITRATE, bitrate);
    }

    private Optional<VideoQualityProfile> findProfileByString(String res) {
        return Arrays.stream(VideoQualityProfile.values())
                .filter(p -> res.contains(String.valueOf(p.getHeight())))
                .findFirst();
    }
}
