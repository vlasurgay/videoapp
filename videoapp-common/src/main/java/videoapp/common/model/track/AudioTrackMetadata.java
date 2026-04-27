package videoapp.common.model.track;

import lombok.Data;

@Data
public class AudioTrackMetadata extends TrackMetadata {
    private String language;
    private String codec;
    private long bitrate;
    private boolean isDefault;

    public static AudioTrackMetadata from(String language, long fileSizeBytes, boolean isDefault) {
        AudioTrackMetadata metadata = new AudioTrackMetadata();

        metadata.setLanguage(language);
        metadata.setCodec("aac");
        metadata.setBitrate(128000);
        metadata.setFileSizeBytes(fileSizeBytes);
        metadata.setDefault(isDefault);

        return metadata;
    }
}
