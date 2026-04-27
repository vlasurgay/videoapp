package videoapp.common.utils;

import java.util.UUID;

public class Utils {

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static int ceil(long a, long b) {
        return (int) (a / b + (a % b == 0 ? 0 : 1));
    }
}
