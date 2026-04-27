package videoapp.common.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "s3_source_key", length = 500)
    private String s3SourceKey;

    @Column(name = "master_playlist_key", length = 500)
    private String masterPlaylistKey;

    @Column(name = "separated_video_key", length = 500)
    private String separatedVideoKey;

    @Column(name = "separated_audio_key", length = 500)
    private String separatedAudioKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_settings", columnDefinition = "jsonb")
    private TargetSettings targetSettings;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
