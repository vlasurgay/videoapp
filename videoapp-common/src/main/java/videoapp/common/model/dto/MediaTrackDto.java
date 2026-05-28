package videoapp.common.model.dto;

import videoapp.common.model.enums.TrackType;

public record MediaTrackDto(
        TrackType type,
        String label,
        String uploadKey
) {
}
