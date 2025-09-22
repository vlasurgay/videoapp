package vlsurhai.common.model.video;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("generated")
public class GeneratedMetadata extends VideoMetadata {
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private VideoFile video;
}
