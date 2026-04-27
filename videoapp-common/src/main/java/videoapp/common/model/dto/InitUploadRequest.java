package videoapp.common.model.dto;

public record InitUploadRequest(
        String fileName,
        String title,
        String description,
        Long fileSizeBytes,
        TargetSettings targetSettings
) {
}
