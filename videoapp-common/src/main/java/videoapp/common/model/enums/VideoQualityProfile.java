package videoapp.common.model.processing;

public enum VideoQualityProfile {
    QUALITY_1080(1920, 1080, "5000"),
    QUALITY_720(1280, 720, "2500"),
    QUALITY_480(854, 480, "1000"),
    QUALITY_360(640, 360, "600"),
    QUALITY_240(426, 240, "300");

    private final int width;
    private final int height;
    private final String bitrateKbps;

    VideoQualityProfile(int width, int height, String bitrateKbps) {
        this.width = width;
        this.height = height;
        this.bitrateKbps = bitrateKbps;
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

}
