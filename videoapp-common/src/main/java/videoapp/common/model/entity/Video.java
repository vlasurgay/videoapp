package videoapp.common.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import videoapp.common.model.dto.TargetSettings;
import videoapp.common.model.enums.VideoStatus;

import java.time.Instant;

@Data
@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 50)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private VideoStatus status = VideoStatus.UPLOADING;

    @Column(name = "source_video_key", length = 500)
    private String sourceVideoKey;

    @Column(name = "source_audio_key", length = 500)
    private String sourceAudioKey;

    @Column(name = "master_playlist_key", length = 500)
    private String masterPlaylistKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_settings", columnDefinition = "jsonb")
    private TargetSettings targetSettings;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
