package vlsurhai.common.model.video;

import jakarta.persistence.*;
import vlsurhai.common.model.*;

import java.util.List;

@Entity
@Table(name = "video_files")
public class VideoFile extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "videoFile", cascade = CascadeType.ALL)
    private UploadInfo uploadInfo;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "creation_time")
    private Long creationTime;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<GeneratedMetadata> generatedMetadata;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "original_metadata_id", unique = true)
    private OriginalMetadata originalMetadata;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UploadInfo getUploadInfo() {
        return uploadInfo;
    }

    public void setUploadInfo(UploadInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public List<GeneratedMetadata> getGeneratedMetadata() {
        return generatedMetadata;
    }

    public void setGeneratedMetadata(List<GeneratedMetadata> generatedMetadata) {
        this.generatedMetadata = generatedMetadata;
    }

    public OriginalMetadata getOriginalMetadata() {
        return originalMetadata;
    }

    public void setOriginalMetadata(OriginalMetadata originalMetadata) {
        this.originalMetadata = originalMetadata;
    }
}
