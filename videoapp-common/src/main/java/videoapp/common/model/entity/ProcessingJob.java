package videoapp.common.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import videoapp.common.model.enums.JobStatus;
import videoapp.common.model.enums.JobType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "processing_jobs")
public class ProcessingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false)
    private Long videoId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private JobType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private JobStatus status = JobStatus.PENDING;

    @ManyToMany
    @JoinTable(
            name = "job_dependencies",
            joinColumns = @JoinColumn(name = "dependent_job_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_job_id")
    )
    private List<ProcessingJob> dependencies = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "attempt")
    private Integer attempt = 0;

    @Column(name = "max_attempts")
    private Integer maxAttempts = 3;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    public void complete() {
        this.status = JobStatus.COMPLETED;
        this.lastError = null;
    }

    public void fail(String error, int retryDelaySeconds) {
        this.lastError = error;

        if (this.attempt >= this.maxAttempts) {
            this.status = JobStatus.FAILED;
        } else {
            this.status = JobStatus.RETRY_WAIT;
            scheduleNextRetry(retryDelaySeconds);
        }
    }

    protected void scheduleNextRetry(int retryDelaySeconds) {
        this.nextRetryAt = Instant.now().plusSeconds(retryDelaySeconds);
    }
}
