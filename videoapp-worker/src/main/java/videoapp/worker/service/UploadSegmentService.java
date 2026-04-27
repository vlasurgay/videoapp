package videoapp.worker.service;

import org.springframework.stereotype.Service;
import videoapp.common.model.processing.UploadStats;
import videoapp.storage.api.StorageProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static videoapp.common.Constants.*;

@Service
public class UploadSegmentService {
    private final StorageProvider storageProvider;

    public UploadSegmentService(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    public Map<String, UploadStats> uploadFiles(Path rootDir, String baseUploadKey, Supplier<Boolean> isRunning) {
        Map<String, UploadStats> stats = new HashMap<>();
        do {
            walkFiles(rootDir, baseUploadKey, stats);
            sleep();
        } while (isRunning.get());

        walkFiles(rootDir, baseUploadKey, stats);

        return stats;
    }

    private void walkFiles(Path rootDir, String baseUploadKey, Map<String, UploadStats> stats) {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(this::isFileCompleted)
                    .forEach(file -> processFile(rootDir, file, baseUploadKey, stats));

        } catch (IOException ignored) {}
    }

    private void processFile(Path rootDir, Path file, String baseUploadKey, Map<String, UploadStats> statsMap) {
        try {
            String relativePath = rootDir.relativize(file).toString().replace("\\", "/");
            String uploadKey = String.format("%s/%s", baseUploadKey, relativePath);
            String label = extractLabel(rootDir, file);

            storageProvider.putObject(uploadKey, file);

            UploadStats stats = statsMap.computeIfAbsent(label, k -> new UploadStats());
            stats.addBytes(Files.size(file));

            if (isMainFile(file)) {
                stats.setFileUploadKey(uploadKey);
            }
            Files.deleteIfExists(file);
        } catch (Exception e) {}
    }

    private String extractLabel(Path rootDir, Path file) {
        Path relative = rootDir.relativize(file);
        return relative.getNameCount() > 1 ? relative.getName(0).toString() : ORIGINAL;
    }

    private boolean isMainFile(Path file) {
        String name = file.toString();
        return name.endsWith(M3U8_EXTENSION) || name.endsWith(M4A_EXTENSION) || name.endsWith(MP3_EXTENSION);
    }

    private boolean isFileCompleted(Path file) {
        return !file.toString().endsWith(TMP_EXTENSION);
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
