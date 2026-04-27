package videoapp.common.utils;

import org.apache.logging.log4j.util.Strings;

public class UploadKeyUtils {

    public static String extractFileNameFromUploadKey(String key) {
        if (Strings.isEmpty(key)) {
            return "";
        }
        int lastSlashIndex = key.lastIndexOf('/');
        return lastSlashIndex != -1 ? key.substring(lastSlashIndex + 1) : key;
    }
}
