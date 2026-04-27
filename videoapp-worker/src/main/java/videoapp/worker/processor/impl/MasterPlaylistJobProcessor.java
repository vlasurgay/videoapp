package videoapp.worker.processor.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.entity.MediaTrack;
import videoapp.common.model.entity.ProcessingJob;
import videoapp.common.model.enums.JobType;
import videoapp.common.model.enums.TrackType;
import videoapp.common.model.track.AudioTrackMetadata;
import videoapp.common.model.track.SubtitleTrackMetadata;
import videoapp.common.model.track.VideoTrackMetadata;
import videoapp.core.service.MediaTrackService;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;
import videoapp.storage.api.StorageProvider;
import videoapp.worker.processor.JobProcessor;

import java.util.List;

import static videoapp.common.Constants.*;
import static videoapp.common.model.enums.JobType.GENERATE_MASTER_PLAYLIST;
import static videoapp.common.utils.JsonNodeExtractor.extractString;

@Slf4j
@Component
public class MasterPlaylistJobProcessor implements JobProcessor {

    private final MediaTrackService trackService;
    private final VideoService videoService;
    private final StorageProvider storageProvider;
    private final PathResolver pathResolver;

    public MasterPlaylistJobProcessor(MediaTrackService trackService,
                                      VideoService videoService,
                                      StorageProvider storageProvider, PathResolver pathResolver) {
        this.trackService = trackService;
        this.videoService = videoService;
        this.storageProvider = storageProvider;
        this.pathResolver = pathResolver;
    }

    @Override
    public JobType getType() {
        return GENERATE_MASTER_PLAYLIST;
    }

    @Override
    public void process(ProcessingJob job) {
        String publicId = extractString(job.getPayload(), PUBLIC_ID);
        String uploadDir = pathResolver.buildBaseHlsDirKey(publicId);
        String uploadKey = String.format("%s/%s%s", uploadDir, MASTER_FILENAME, M3U8_EXTENSION);

        List<MediaTrack> tracks = trackService.findAllByVideoId(job.getVideoId());
        String masterPlaylistContent = buildMasterM3U8(tracks);

        storageProvider.putObject(uploadKey, masterPlaylistContent.getBytes(), APPLICATION_X_MPEGURL);

        videoService.updateMasterPlaylistKey(publicId, uploadKey);

        log.info("Video processing successfully completed. Master playlist has been created, publicId={}", publicId);
    }

    private String buildMasterM3U8(List<MediaTrack> tracks) {
        StringBuilder sb = new StringBuilder("#EXTM3U\n#EXT-X-VERSION:3\n\n");

        List<MediaTrack> audioTracks = tracks.stream()
                .filter(t -> t.getType() == TrackType.AUDIO)
                .toList();
        appendAudio(audioTracks, sb);


        List<MediaTrack> subtitleTracks = tracks.stream()
                .filter(t -> t.getType() == TrackType.SUBTITLE)
                .toList();
        appendSubtitles(subtitleTracks, sb);

        if (!audioTracks.isEmpty() || !subtitleTracks.isEmpty()) {
            sb.append("\n");
        }

        List<MediaTrack> videoTracks = tracks.stream()
                .filter(t -> t.getType() == TrackType.VIDEO)
                .toList();
        appendVideo(videoTracks, audioTracks, subtitleTracks, sb);

        return sb.toString();
    }

    private void appendAudio(List<MediaTrack> audioTracks, StringBuilder sb) {
        for (MediaTrack audio : audioTracks) {
            AudioTrackMetadata meta = (AudioTrackMetadata) audio.getMetadata();
            String relativeUri = audio.getLabel() + "/" + PLAYLIST_FILENAME + M3U8_EXTENSION;

            sb.append(String.format(
                    "#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID=\"audio\",NAME=\"%s\",LANGUAGE=\"%s\",DEFAULT=YES,AUTOSELECT=YES,URI=\"%s\"\n",
                    audio.getLabel(),
                    meta.getLanguage() != null ? meta.getLanguage() : "en",
                    relativeUri
            ));
        }
    }

    private void appendSubtitles(List<MediaTrack> subtitleTracks, StringBuilder sb) {
        for (MediaTrack sub : subtitleTracks) {
            SubtitleTrackMetadata meta = (SubtitleTrackMetadata) sub.getMetadata();
            String relativeUri = sub.getLabel() + "/" + PLAYLIST_FILENAME + M3U8_EXTENSION;

            sb.append(String.format(
                    "#EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID=\"subtitles\",NAME=\"%s\",LANGUAGE=\"%s\",DEFAULT=NO,AUTOSELECT=YES,FORCED=NO,URI=\"%s\"\n",
                    sub.getLabel(),
                    meta.getLanguage() != null ? meta.getLanguage() : "en",
                    relativeUri
            ));
        }
    }

    private void appendVideo(List<MediaTrack> videoTracks, List<MediaTrack> audioTracks, List<MediaTrack> subtitleTracks, StringBuilder sb) {
        for (MediaTrack track : videoTracks) {
            VideoTrackMetadata meta = (VideoTrackMetadata) track.getMetadata();

            sb.append(
                    String.format("#EXT-X-STREAM-INF:BANDWIDTH=%s,RESOLUTION=%sx%s,NAME=\"%s\"",
                            meta.getBitrate(), meta.getWidth(), meta.getHeight(), track.getLabel())
            );

            if (!audioTracks.isEmpty()) {
                sb.append(",AUDIO=\"audio\"");
            }

            if (!subtitleTracks.isEmpty()) {
                sb.append(",SUBTITLES=\"subtitles\"");
            }

            sb.append("\n");
            sb.append(track.getLabel()).append("/").append(PLAYLIST_FILENAME).append(M3U8_EXTENSION).append("\n\n");
        }
    }
}
