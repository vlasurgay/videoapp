package videoapp.worker.integration.ffmpeg;

import org.apache.logging.log4j.util.Strings;
import videoapp.common.model.dto.Resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static videoapp.common.Constants.*;

public class FfmpegCommandBuilder {

    private String sourceUrl;
    private String outputFilesDirectory;
    private String outputAudioFileName;
    private List<Resolution> profiles;
    private Mode mode = null;

    public FfmpegCommandBuilder setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public FfmpegCommandBuilder setOutputFilesDirectory(String outputFilesDirectory) {
        this.outputFilesDirectory = outputFilesDirectory;
        return this;
    }

    public FfmpegCommandBuilder setOutputAudioFileName(String outputAudioFileName) {
        this.outputAudioFileName = outputAudioFileName;
        return this;
    }

    public FfmpegCommandBuilder setTargetQualityProfiles(List<Resolution> profiles) {
        this.profiles = profiles;
        return this;
    }

    public FfmpegCommandBuilder setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public List<String> build() {
        if (Strings.isBlank(sourceUrl)) {
            throw new IllegalStateException("Source URL is required to build FFmpeg command");
        }
        if (mode == null) {
            throw new IllegalStateException("Mode is required to build FFmpeg command");
        }

        List<String> commands = new ArrayList<>(List.of(
                "ffmpeg",
                "-hide_banner",
                "-loglevel", "error",
                "-threads", "0",
                "-i", sourceUrl
        ));

        mode.function.accept(this, commands);
        return commands;
    }


    private void buildHlsVideoArguments(List<String> commands) {
        for (int i = 0; i < profiles.size(); i++) {
            Resolution profile = profiles.get(i);

            commands.addAll(List.of(
                    "-map", "0:v:0",
                    "-c:v:" + i, "libx264",
                    "-crf", "23",
                    "-preset", "veryfast",
                    "-maxrate:v:" + i, String.valueOf(profile.bitrate()),
                    "-bufsize:v:" + i, String.valueOf(profile.bitrate()),
                    "-s:v:" + i, profile.width() + "x" + profile.height()
            ));
        }
        commands.addAll(List.of(
                "-f", "hls",
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-hls_flags", "temp_file",
//                "-master_pl_name", "master.m3u8",
                "-hls_segment_filename", outputFilesDirectory + "/%v/segment_%05d.ts",
                "-var_stream_map", buildStreamMap(),
                outputFilesDirectory + "/%v/" + PLAYLIST_FILENAME + M3U8_EXTENSION
        ));
    }

    private String buildStreamMap() {
        return IntStream.range(0, profiles.size())
                .mapToObj(i -> String.format("v:%s,name:%s", i, profiles.get(i).label()))
                .collect(Collectors.joining(" "));
    }

    private void buildExtractAudioArgs(List<String> cmd) {
        cmd.addAll(List.of("-vn", "-acodec", "aac", outputFilesDirectory + "/" + outputAudioFileName + M4A_EXTENSION));
    }

    public enum Mode {
        HLS_VIDEO(FfmpegCommandBuilder::buildHlsVideoArguments),
        EXTRACT_AUDIO(FfmpegCommandBuilder::buildExtractAudioArgs);

        private final BiConsumer<FfmpegCommandBuilder, List<String>> function;

        Mode(BiConsumer<FfmpegCommandBuilder, List<String>> function) {
            this.function = function;
        }
    }
}
