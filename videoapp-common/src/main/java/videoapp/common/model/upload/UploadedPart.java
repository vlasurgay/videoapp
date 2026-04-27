package videoapp.common.model.upload;


public record UploadedPart(
        int partNumber,
        String eTag
) {
}
