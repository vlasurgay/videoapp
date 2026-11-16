package videoapp.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "videoapp.common")
public record CommonProperties(
        boolean aiModeEnabled
) {
}
