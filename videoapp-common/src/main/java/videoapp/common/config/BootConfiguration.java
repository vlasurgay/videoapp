package vlsurhai.common.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "vlsurhai.common.model")
@EnableJpaRepositories(basePackages = "vlsurhai.storage.jpa")
public class BootConfiguration {
}
