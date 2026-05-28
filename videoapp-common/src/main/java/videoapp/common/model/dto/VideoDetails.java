package videoapp.common.model.dto;

import java.util.List;

public record VideoDetails(
        String publicId,
        String title,
        String description,
        String masterPlaylistKey,
        List<MediaTrackDto> mediaTracks
) {
}
