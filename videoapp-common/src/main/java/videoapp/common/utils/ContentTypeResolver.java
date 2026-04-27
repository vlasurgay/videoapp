package videoapp.common.utils;

import java.nio.file.Path;

public class ContentTypeResolver {

    public static String resolveContentType(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = getFileExtension(fileName);

        return switch (extension.toLowerCase()) {
            case "mp4" -> "video/mp4";
            case "mkv" -> "video/x-matroska";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "m3u8" -> "application/vnd.apple.mpegurl";
            case "ts" -> "video/mp2t";
            case "m4a" -> "audio/mp4";
            default -> "application/octet-stream";
        };
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
