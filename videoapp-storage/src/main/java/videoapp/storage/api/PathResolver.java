package videoapp.storage.api;

public interface PathResolver {

    String buildTempFileKey(String publicId, String fileName);

    String buildSourceFileKey(String publicId, String fileName);

    String buildSourceDirKey(String publicId);

    String buildBaseHlsDirKey(String publicId);

    String buildClientsKey(String subKey);

}
