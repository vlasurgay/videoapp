package vlsurhai.common.model.video;

import jakarta.persistence.*;
import vlsurhai.common.model.BaseEntity;

@Entity
@Table(name = "video_metadata")
@DiscriminatorColumn(name = "type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class VideoMetadata extends BaseEntity {

    @Column(insertable = false, updatable = false)
    private String type;

    private String format;
    private Double duration;
    private Integer height;
    private Integer width;
    private Long bitrate;

    @Column(name = "has_audio")
    private Boolean hasAudio;

    @Column(name = "file_size_in_bytes")
    private Long fileSizeBytes;

    @Column(name = "s3_key")
    private String s3Key;


    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Long getBitrate() {
        return bitrate;
    }

    public void setBitrate(Long bitrate) {
        this.bitrate = bitrate;
    }


    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public Boolean getHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(Boolean hasAudio) {
        this.hasAudio = hasAudio;
    }
}
