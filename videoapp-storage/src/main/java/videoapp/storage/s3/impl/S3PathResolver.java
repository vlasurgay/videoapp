package videoapp.storage.s3.impl;

import org.springframework.stereotype.Component;
import videoapp.storage.api.PathResolver;

@Component
public class S3PathResolver implements PathResolver {

    @Override
    public String buildTempFileKey(String publicId, String fileName) {
        return String.format("temp/uploads/%s/%s", publicId, fileName);
    }

    @Override
    public String buildSourceFileKey(String publicId, String fileName) {
        return String.format("videos/%s/source/%s", publicId, fileName);
    }

    @Override
    public String buildSourceDirKey(String publicId) {
        return String.format("videos/%s/source", publicId);
    }

    @Override
    public String buildBaseHlsDirKey(String publicId) {
        return String.format("videos/%s/hls", publicId);
    }
}
