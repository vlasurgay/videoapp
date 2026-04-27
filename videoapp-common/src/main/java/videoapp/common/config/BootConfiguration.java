package videoapp.common.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "videoapp.common.model")
@EnableJpaRepositories(basePackages = "videoapp.storage.jpa")
@ConfigurationPropertiesScan(basePackages = "videoapp")
public class BootConfiguration {
}
