package videoapp.worker.integration.ffmpeg;

import org.springframework.stereotype.Component;
import videoapp.storage.s3.S3PresignedUrlProvider;
import videoapp.storage.s3.http.HttpPresignedUrlUploader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static videoapp.common.Constants.M3U8_EXTENSION;
import static videoapp.common.Constants.TS_EXTENSION;

@Component
public class SegmentUploader {
    private final S3PresignedUrlProvider urlProvider;
    private final HttpPresignedUrlUploader presignedUrlUploader;

    public SegmentUploader(S3PresignedUrlProvider urlProvider, HttpPresignedUrlUploader presignedUrlUploader) {
        this.urlProvider = urlProvider;
        this.presignedUrlUploader = presignedUrlUploader;
    }

    public void uploadFiles(Path rootDir, String baseS3Key, Supplier<Boolean> isRunning) {
        do {
            walkFiles(rootDir, baseS3Key, TS_EXTENSION);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (isRunning.get());
        walkFiles(rootDir, baseS3Key, TS_EXTENSION);
        walkFiles(rootDir, baseS3Key, M3U8_EXTENSION);
    }

    private void walkFiles(Path rootDir, String baseS3Key, String extension) {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(extension))
                    .forEach(file -> uploadSingleFile(rootDir, file, baseS3Key));
        } catch (IOException ignored) {}
    }

    private void uploadSingleFile(Path rootDirectory, Path file, String baseS3Key) {
        try {
            String relativePath = rootDirectory.relativize(file).toString().replace("\\", "/");
            String s3Key = baseS3Key + "/" + relativePath;

            String url = urlProvider.presignPutObject(s3Key).url().toString();
            presignedUrlUploader.upload(url, Files.readAllBytes(file));

            Files.deleteIfExists(file);
        } catch (Exception ignored) {}
    }
}
