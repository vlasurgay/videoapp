package videoapp.common.model.presign;

import java.util.List;

public record CompletedMultipartContext(
        String key,
        String uploadId,
        List<UploadedPart> uploadedParts
) {
}
