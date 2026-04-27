package videoapp.worker.integration.ffprobe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import videoapp.common.model.dto.VideoMetadata;

import java.nio.charset.StandardCharsets;

@Component
public class FfprobeClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoMetadata probe(String sourceUrl) {
        try {
            Process process = buildProcess(sourceUrl);

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("ffprobe failed: " + output);
            }
            if (output.isBlank()) {
                throw new RuntimeException("ffprobe output is empty");
            }

            return parse(output);

        } catch (Exception e) {
            throw new RuntimeException("ffprobe execution failed", e);
        }
    }

    private Process buildProcess(String sourceUrl) throws Exception {
        return new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-hide_banner",
                "-show_format",
                "-show_streams",
                "-of", "json",
                "-show_entries",
                "format=format_name,duration,bit_rate,size:stream=width,height,codec_type",
                sourceUrl
        )
                .redirectErrorStream(true)
                .start();
    }

    private VideoMetadata parse(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        JsonNode format = root.path("format");
        JsonNode streams = root.path("streams");

        JsonNode videoStream = null;
        boolean hasAudio = false;

        for (JsonNode stream : streams) {
            String type = stream.path("codec_type").asText();

            if ("video".equals(type)) {
                videoStream = stream;
            } else if ("audio".equals(type)) {
                hasAudio = true;
            }
        }

        if (videoStream == null) {
            throw new RuntimeException("No video stream found");
        }

        String rawFormat = format.path("format_name").asText("");
        String normalizedFormat = rawFormat.contains(",")
                ? rawFormat.substring(0, rawFormat.indexOf(','))
                : rawFormat;

        return new VideoMetadata(
                normalizedFormat,
                format.path("duration").asDouble(0.0),
                videoStream.path("height").asInt(0),
                videoStream.path("width").asInt(0),
                format.path("bit_rate").asLong(0),
                hasAudio,
                format.path("size").asLong(0)
        );
    }
}
