package videoapp.common.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import videoapp.common.model.enums.TrackType;
import videoapp.common.model.track.TrackMetadata;

import java.time.Instant;

@Data
@Entity
@Table(name = "media_tracks")
public class MediaTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TrackType type;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(name = "upload_key", nullable = false, length = 500)
    private String uploadKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private TrackMetadata metadata;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
