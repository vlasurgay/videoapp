package videoapp.common.model.dto;

public record VideoMetadataDto(
        String format,
        Double duration,
        Integer height,
        Integer width,
        Long bitrate,
        Boolean hasAudio,
        Long fileSizeBytes
) {
}
