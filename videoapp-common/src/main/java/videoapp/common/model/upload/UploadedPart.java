package videoapp.common.model.presign;


public record UploadedPart(
        int partNumber,
        String eTag
) {
}
