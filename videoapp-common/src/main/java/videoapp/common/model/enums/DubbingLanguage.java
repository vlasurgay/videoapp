package videoapp.common.model.enums;

import java.util.Arrays;
import java.util.List;

public enum DubbingLanguage {

    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    PORTUGUESE("pt", "Portuguese"),
    POLISH("pl", "Polish"),
    TURKISH("tr", "Turkish"),
    RUSSIAN("ru", "Russian"),
    DUTCH("nl", "Dutch"),
    CZECH("cs", "Czech"),
    ARABIC("ar", "Arabic"),
    CHINESE("zh-cn", "Chinese"),
    JAPANESE("ja", "Japanese"),
    HUNGARIAN("hu", "Hungarian"),
    KOREAN("ko", "Korean"),
    HINDI("hi", "Hindi");

    private final String code;
    private final String displayName;

    DubbingLanguage(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String getDisplayNameByCode(String code) {
        return Arrays.stream(values())
                .filter(lang -> lang.code.equalsIgnoreCase(code))
                .map(DubbingLanguage::getDisplayName)
                .findFirst()
                .orElse(null);
    }

    public static List<String> availableCodes() {
        return Arrays.stream(values()).map(DubbingLanguage::getCode).toList();
    }

    public static boolean isSupported(String code) {
        return code != null && Arrays.stream(values()).anyMatch(l -> l.code.equalsIgnoreCase(code));
    }
}
