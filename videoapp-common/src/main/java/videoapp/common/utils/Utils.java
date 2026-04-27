package videoapp.common.utils;

import org.apache.logging.log4j.util.Strings;

import java.util.UUID;

public class VideoUtils {

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static String extractFileNameFromUploadKey(String key) {
        if (Strings.isEmpty(key)) {
            return "";
        }
        int lastSlashIndex = key.lastIndexOf('/');
        return lastSlashIndex != -1 ? key.substring(lastSlashIndex + 1) : key;
    }

    public static int ceil(long a, long b) {
        return (int) (a / b + (a % b == 0 ? 0 : 1));
    }
}
