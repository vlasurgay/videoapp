package videoapp.common.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetSettings implements Serializable {

    @JsonProperty("target_resolutions")
    private List<String> targetResolutions;

    private Boolean muted;

    @JsonProperty("ai_subs")
    private Boolean aiSubs;
}
