package videoapp.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class ProcessingWorkspace implements AutoCloseable {

    private final Path path;

    public ProcessingWorkspace(String basePath) throws IOException {
        String normalizedPath = Path.of(basePath).toAbsolutePath().normalize().toString();
        String uniqueWorkspaceName = buildUniqueWorkspaceName();
        this.path = Files.createDirectories(Path.of(normalizedPath, uniqueWorkspaceName));
    }

    public Path getPath() {
        return path;
    }

    public String getAbsolutePath() {
        return path.toAbsolutePath().toString();
    }

    private String buildUniqueWorkspaceName() {
        return String.format("work-%s-%s", System.currentTimeMillis(), UUID.randomUUID().toString().substring(0, 8));
    }

    @Override
    public void close() {
        try {
            FileUtils.deleteDirectory(path.toFile());
            log.debug("Directory successfully deleted: {}", path);
        } catch (IOException e) {
            log.warn("Cannot delete directory {}", path, e);
        }
    }
}

