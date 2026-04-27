package videoapp.common.model.dto;

public record Resolution(
        int height,
        int width,
        long bitrate,
        String label
) {
}
