package videoapp.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "videoapp.worker")
public record WorkerProperties(
        String temporalOutputDirectory,
        int jobNextRetryDelaySec
) {
}
