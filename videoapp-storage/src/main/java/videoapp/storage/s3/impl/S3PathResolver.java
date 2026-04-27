package videoapp.storage;

public class StoragePathResolver {

    public static String buildTempFileKey(String publicId, String fileName) {
        return String.format("temp/uploads/%s/%s", publicId, fileName);
    }

    public static String buildSourceFileKey(String publicId, String fileName) {
        return String.format("videos/%s/source/%s", publicId, fileName);
    }

    public static String buildSourceDirKey(String publicId) {
        return String.format("videos/%s/source", publicId);
    }

    public static String buildBaseHlsDirKey(String publicId) {
        return String.format("videos/%s/hls", publicId);
    }
}
