package videoapp.common.model.presign;

import java.util.Map;

public record MultipartPresignedUrl(
         int partNumber,
         String url,
         Map<String, String> headers
) {
}
