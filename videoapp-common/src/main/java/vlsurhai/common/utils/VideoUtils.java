package vlsurhai.common.utils;

import java.util.UUID;

public class VideoUtils {

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
