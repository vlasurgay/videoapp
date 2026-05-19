package videoapp.common.model.enums;

import static videoapp.common.Constants.*;

public enum VideoQualityProfile {
    QUALITY_1080(1080, "5000", QUALITY_1080P),
    QUALITY_720(720, "2500", QUALITY_720P),
    QUALITY_480(480, "1000", QUALITY_480P),
    QUALITY_360(360, "600", QUALITY_360P),
    QUALITY_240(240, "300", QUALITY_240P);

    private final int height;
    private final String bitrateKbps;
    private final String label;

    VideoQualityProfile(int height, String bitrateKbps, String label) {
        this.height = height;
        this.bitrateKbps = bitrateKbps;
        this.label = label;
    }

    public int getHeight() {
        return height;
    }

    public String getBitrateKbps() {
        return bitrateKbps;
    }

    public String getLabel() {
        return label;
    }
}
