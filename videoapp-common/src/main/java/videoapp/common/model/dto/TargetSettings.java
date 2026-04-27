package videoapp.common.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

import static videoapp.common.Constants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetSettings implements Serializable {

    @JsonProperty(TARGET_RESOLUTIONS)
    private List<String> targetResolutions;

    @JsonProperty(MUTED)
    private Boolean muted;

    @JsonProperty(AI_SUBS)
    private Boolean aiSubs;
}
