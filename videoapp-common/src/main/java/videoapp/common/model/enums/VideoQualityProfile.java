package videoapp.common.model.enums;

import static videoapp.common.Constants.*;

public enum VideoQualityProfile {
    QUALITY_1080(1920, 1080, "5000", QUALITY_1080P),
    QUALITY_720(1280, 720, "2500", QUALITY_720P),
    QUALITY_480(854, 480, "1000", QUALITY_480P),
    QUALITY_360(640, 360, "600", QUALITY_360P),
    QUALITY_240(426, 240, "300", QUALITY_240P);

    private final int width;
    private final int height;
    private final String bitrateKbps;
    private final String label;

    VideoQualityProfile(int width, int height, String bitrateKbps, String label) {
        this.width = width;
        this.height = height;
        this.bitrateKbps = bitrateKbps;
        this.label = label;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getBitrateKbps() {
        return bitrateKbps;
    }

    public String getLabel() {
        return label;
    }
}
