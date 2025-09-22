package vlsurhai.common.model.video;

import jakarta.persistence.*;
import vlsurhai.common.model.BaseEntity;
import vlsurhai.common.model.User;

@Entity
@Table(name = "upload_info")
public class UploadInfo extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "video_id")
    private VideoFile videoFile;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "upload_id")
    private String uploadId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "creation_time", nullable = false)
    private long creationTime;

    @Column(name = "expires_at", nullable = false)
    private long expiresAt;

    @Column(name = "total_parts", nullable = false)
    private int totalParts;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "original_metadata_id", unique = true)
    private OriginalMetadata originalMetadata;

    @Embedded
    private TransformRequest transformRequest;

    @Enumerated(EnumType.STRING)
    private UploadStatus status;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(int totalParts) {
        this.totalParts = totalParts;
    }

    public OriginalMetadata getOriginalMetadata() {
        return originalMetadata;
    }

    public void setOriginalMetadata(OriginalMetadata originalMetadata) {
        this.originalMetadata = originalMetadata;
    }

    public TransformRequest getTransformRequest() {
        return transformRequest;
    }

    public void setTransformRequest(TransformRequest transformRequest) {
        this.transformRequest = transformRequest;
    }

    public UploadStatus getStatus() {
        return status;
    }

    public void setStatus(UploadStatus status) {
        this.status = status;
    }


    public VideoFile getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(VideoFile videoFile) {
        this.videoFile = videoFile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
