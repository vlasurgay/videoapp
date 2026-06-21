package videoapp.common.model.dto;

import java.util.List;

public record VideoConfigDto(
        List<String> availableResolutions,
        List<String> availableLanguages
) {
}
