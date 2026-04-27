package videoapp.worker.ffmpeg;

import videoapp.common.model.dto.Resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FfmpegCommandBuilder {

    private String originS3Key;
    private String outputDir;
    private List<Resolution> profiles;

    public FfmpegCommandBuilder setOriginS3Key(String originS3Key) {
        this.originS3Key = originS3Key;
        return this;
    }

    public FfmpegCommandBuilder setOutputDirectory(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    public FfmpegCommandBuilder setTargetQualityProfiles(List<Resolution> profiles) {
        this.profiles = profiles;
        return this;
    }

    public List<String> build() {
        List<String> cmd = new ArrayList<>(List.of(
                "ffmpeg", "-hide_banner", "-v", "error", "-i", originS3Key
        ));

        for (int i = 0; i < profiles.size(); i++) {

            Resolution profile = profiles.get(i);

            cmd.addAll(List.of("-map", "0:v:0", "-map", "0:a:0"));
//            cmd.addAll(List.of("-c:v:" + i, "libx264", "-b:v:" + i, profile.bitrate()));
            cmd.addAll(List.of(
                    "-c:v:" + i, "libx264", "-crf", "23", "-maxrate:v:" + i, profile.bitrate(),
                    "-bufsize:v:" + i, (Long.parseLong(profile.bitrate()) * 2) + ""
            ));
            cmd.addAll(List.of("-s:v:" + i, profile.width() + "x" + profile.height()));
            cmd.addAll(List.of("-c:a:" + i, "aac", "-b:a:" + i, "128k"));
        }

        cmd.addAll(List.of(
                "-f", "hls", "-hls_time", "6", "-hls_playlist_type", "vod",
                "-master_pl_name", "master.m3u8",
                "-hls_segment_filename", outputDir + "/%v/segment_%05d.ts",
                "-var_stream_map", buildStreamMap(),
                outputDir + "/%v/playlist.m3u8"
        ));

        return cmd;
    }

    private String buildStreamMap() {
        return IntStream.range(0, profiles.size())
                .mapToObj(i -> String.format("v:%d,a:%d,name:%dp", i, i, profiles.get(i).height()))
                .collect(Collectors.joining(" "));
    }
}
