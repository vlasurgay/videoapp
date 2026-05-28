package videoapp.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import videoapp.common.model.dto.MediaTrackDto;
import videoapp.common.model.dto.VideoDetails;
import videoapp.common.model.entity.MediaTrack;
import videoapp.common.model.entity.Video;
import videoapp.core.service.MediaTrackService;
import videoapp.core.service.VideoService;
import videoapp.storage.api.PathResolver;

import java.util.List;

@Slf4j
@Service
public class VideoQueryService {

    private final VideoService videoService;
    private final MediaTrackService mediaTrackService;
    private final PathResolver pathResolver;

    public VideoQueryService(VideoService videoService, MediaTrackService mediaTrackService, PathResolver pathResolver) {
        this.videoService = videoService;
        this.mediaTrackService = mediaTrackService;
        this.pathResolver = pathResolver;
    }

    public VideoDetails getVideoByPublicId(String publicId) {
        Video video = videoService.findByPublicId(publicId);

        String clientsKey = pathResolver.buildClientsKey(video.getMasterPlaylistKey());
        List<MediaTrack> mediaTracks = mediaTrackService.findAllByVideoId(video.getId());
        List<MediaTrackDto> mediaTrackDtos = convertMediaTrack(mediaTracks);

        return new VideoDetails(
                publicId, video.getTitle(), video.getDescription(), clientsKey, mediaTrackDtos
        );
    }

    private List<MediaTrackDto> convertMediaTrack(List<MediaTrack> mediaTracks) {
        return mediaTracks.stream()
                .map(mediaTrack -> {
                    String clientsKey = pathResolver.buildClientsKey(mediaTrack.getPlaylistKey());
                    return new MediaTrackDto(mediaTrack.getType(), mediaTrack.getLabel(), clientsKey);
                })
                .toList();
    }
}
