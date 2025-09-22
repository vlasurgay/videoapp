package vlsurhai.common.model.video;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("original")
public class OriginalMetadata extends VideoMetadata {
}
