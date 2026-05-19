package videoapp.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import videoapp.common.model.dto.InitUploadRequest;
import videoapp.common.model.entity.Video;
import videoapp.common.model.enums.VideoStatus;
import videoapp.storage.jpa.repository.VideoRepository;

import java.time.Instant;

@Slf4j
@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public Video initiateVideo(String publicId, InitUploadRequest request, Instant createdAt) {
        Video video = new Video();

        video.setPublicId(publicId);
        video.setTitle(request.title());
        video.setDescription(request.description());
        video.setCreatedAt(createdAt);
        video.setTargetSettings(request.targetSettings());

        return videoRepository.save(video);
    }

    public Video save(Video video) {
        return videoRepository.save(video);
    }

    public Video findByPublicId(String publicId) {
        return videoRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("No video found by publicId: " + publicId));
    }

    public Video getById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No video found by id: " + id));
    }

    public void updateVideoStatus(String uploadId, VideoStatus status) {
        videoRepository.updateStatusByUploadId(uploadId, status);
    }

    public void updateVideoSourceKey(String publicId, String sourceKey) {
        videoRepository.updateSourceVideoKeyByPublicId(publicId, sourceKey);
    }

    public void updateAudioSourceKey(String publicId, String sourceAudioKey) {
        videoRepository.updateSourceAudioKeyByPublicId(publicId, sourceAudioKey);
    }

    public void completeVideoProcessing(String publicId, String masterPlaylistKey) {
        videoRepository.updateMasterPlaylistKeyAndStatusByPublicId(publicId, VideoStatus.COMPLETED, masterPlaylistKey);
    }
}
