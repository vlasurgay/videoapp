package videoapp.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import videoapp.common.model.entity.MediaTrack;
import videoapp.common.model.enums.TrackType;
import videoapp.common.model.track.TrackMetadata;
import videoapp.storage.jpa.repository.MediaTrackRepository;

import java.util.List;

@Slf4j
@Service
public class MediaTrackService {

    private final MediaTrackRepository mediaTrackRepository;

    public MediaTrackService(MediaTrackRepository mediaTrackRepository) {
        this.mediaTrackRepository = mediaTrackRepository;
    }

    public MediaTrack initializeMediaTrack(Long videoId, TrackType type, String label, String s3Key, TrackMetadata metadata) {
        MediaTrack track = new MediaTrack();

        track.setVideoId(videoId);
        track.setType(type);
        track.setLabel(label);
        track.setUploadKey(s3Key);
        track.setMetadata(metadata);

        return mediaTrackRepository.save(track);
    }

    public List<MediaTrack> findAllByVideoId(Long videoId) {
        return mediaTrackRepository.findByVideoId(videoId);
    }
}
