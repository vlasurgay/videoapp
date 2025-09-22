package vlsurhai.common.model.video;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.List;

@Embeddable
public class TransformRequest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Column(name = "target_formats", columnDefinition = "TEXT")
    private String targetFormats;

    @Column(name = "target_resolutions", columnDefinition = "TEXT")
    private String targetResolutions;

    @Column(name = "target_codecs", columnDefinition = "TEXT")
    private String targetCodecs;

    private Boolean muted;

    public List<String> getTargetFormats() {
        return convertStringToList(targetFormats);
    }

    @JsonSetter("targetFormats")
    public void setTargetFormats(List<String> targetFormats) {
        this.targetFormats = convertListToString(targetFormats);
    }

    public void setTargetFormats(String targetFormats) {
        this.targetFormats = targetFormats;
    }

    public List<String> getTargetResolutions() {
        return convertStringToList(targetResolutions);
    }

    @JsonSetter("targetResolutions")
    public void setTargetResolutions(List<String> targetResolutions) {
        this.targetResolutions = convertListToString(targetResolutions);
    }

    public void setTargetResolutions(String targetResolutions) {
        this.targetResolutions = targetResolutions;
    }

    public List<String> getTargetCodecs() {
        return convertStringToList(targetCodecs);
    }

    @JsonSetter("targetCodecs")
    public void setTargetCodecs(List<String> targetCodecs) {
        this.targetCodecs = convertListToString(targetCodecs);
    }

    public void setTargetCodecs(String targetCodecs) {
        this.targetCodecs = targetCodecs;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(Boolean muted) {
        this.muted = muted;
    }

    private String convertListToString(List<String> targets) {
        try {
            if (targets != null && !targets.isEmpty()) {
                return objectMapper.writeValueAsString(targets);
            }
        } catch (JsonProcessingException e) {}

        return null;
    }

    private List<String> convertStringToList(String targets) {
        try {
            if (targets != null && !targets.isEmpty()) {
                return objectMapper.readValue(targets, new TypeReference<>() {});
            }
        } catch (JsonProcessingException e) {}

        return null;
    }
}
