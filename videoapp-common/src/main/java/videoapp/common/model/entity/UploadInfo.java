package videoapp.common.model.jpa;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import videoapp.common.model.dto.VideoMetadata;

import java.time.Instant;

@Data
@Entity
@Table(name = "upload_infos")
public class UploadInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(name = "upload_id", nullable = false)
    private String uploadId;

    @Column(name = "s3_origin_key", nullable = false, length = 500)
    private String s3OriginKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_metadata", columnDefinition = "jsonb")
    private VideoMetadata baseMetadata;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;
}
