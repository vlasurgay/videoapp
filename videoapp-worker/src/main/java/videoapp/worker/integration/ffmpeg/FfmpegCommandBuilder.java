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
    private double sourceFileStartTimeCodeSec = 0.0;
    private double sourceFileDurationSec = 0.0;
    private String outputFilesDirectory;
    private String outputAudioFileName;
    private List<Resolution> profiles;
    private Mode mode = null;
    private LogLevel logLevel = LogLevel.ERROR;

    public FfmpegCommandBuilder setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public FfmpegCommandBuilder setSourceFileDurationSec(double sourceFileDurationSec) {
        this.sourceFileDurationSec = sourceFileDurationSec;
        return this;
    }

    public FfmpegCommandBuilder setSourceFileStartTimeCodeSec(double sourceFileStartTimeCodeSec) {
        this.sourceFileStartTimeCodeSec = sourceFileStartTimeCodeSec;
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

    public FfmpegCommandBuilder setLogLevel(LogLevel level) {
        this.logLevel = level;
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
                "-loglevel", logLevel.level,
                "-threads", "0"
        ));
        if (sourceFileStartTimeCodeSec > 0) {
            commands.addAll(List.of("-ss", String.valueOf(sourceFileStartTimeCodeSec)));
        }
        commands.addAll(List.of("-i", sourceUrl));

        mode.function.accept(this, commands);
        return commands;
    }

    private void buildHlsAudioArguments(List<String> cmd) {
        cmd.addAll(List.of(
                "-vn",
                "-c:a", "aac",
                "-b:a", "128k",
                "-f", "hls",
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-hls_flags", "temp_file",
                "-hls_segment_filename", outputFilesDirectory + "/segment_%05d.ts",
                outputFilesDirectory + "/" +  PLAYLIST_FILENAME + M3U8_EXTENSION
        ));
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

    private void buildFindSilenceEndArgs(List<String> cmd) {
        cmd.addAll(List.of("-af", "silencedetect=n=-30dB:d=0.5", "-f", "null", "-"));
    }

    private void extractAudioFragment(List<String> cmd) {
        cmd.addAll(List.of("-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1"));
        if (sourceFileDurationSec > 0) {
            cmd.addAll(List.of("-t", String.valueOf(sourceFileDurationSec)));
        }
        cmd.addAll(List.of("-f", "wav", "pipe:1"));
    }

    public enum Mode {
        HLS_AUDIO(FfmpegCommandBuilder::buildHlsAudioArguments),
        HLS_VIDEO(FfmpegCommandBuilder::buildHlsVideoArguments),
        EXTRACT_AUDIO(FfmpegCommandBuilder::buildExtractAudioArgs),
        FIND_SILENCE_END(FfmpegCommandBuilder::buildFindSilenceEndArgs),
        EXTRACT_AUDIO_FRAGMENT(FfmpegCommandBuilder::extractAudioFragment);

        private final BiConsumer<FfmpegCommandBuilder, List<String>> function;

        Mode(BiConsumer<FfmpegCommandBuilder, List<String>> function) {
            this.function = function;
        }
    }

    public enum LogLevel {
        QUIET("quiet"),
        PANIC("panic"),
        FATAL("fatal"),
        ERROR("error"),
        WARNING("warning"),
        INFO("info"),
        VERBOSE("verbose"),
        DEBUG("debug"),
        TRACE("trace");

        private final String level;

        LogLevel(String level) {
            this.level = level;
        }
    }
}
