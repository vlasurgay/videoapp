package videoapp.common.model.upload;

import java.util.List;

public record CompletedMultipartContext(
        String key,
        String uploadId,
        List<UploadedPart> uploadedParts
) {
}
