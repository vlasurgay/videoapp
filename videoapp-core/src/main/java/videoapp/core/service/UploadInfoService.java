package videoapp.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import videoapp.common.model.entity.UploadInfo;
import videoapp.storage.jpa.repository.UploadInfoRepository;

import java.time.Instant;
import java.util.function.Consumer;

@Slf4j
@Service
public class UploadInfoService {

    private final UploadInfoRepository uploadInfoRepository;

    public UploadInfoService(UploadInfoRepository uploadInfoRepository) {
        this.uploadInfoRepository = uploadInfoRepository;
    }

    public UploadInfo initializeUploadInfo(Long videoId, String uploadId, String key, Instant expiresAt, Instant createdAt) {
        UploadInfo uploadInfo = new UploadInfo();

        uploadInfo.setVideoId(videoId);
        uploadInfo.setUploadId(uploadId);
        uploadInfo.setUploadOriginKey(key);
        uploadInfo.setCreatedAt(createdAt);
        uploadInfo.setExpiresAt(expiresAt);

        return uploadInfoRepository.save(uploadInfo);
    }

    public UploadInfo updateByVideoId(Long videoId, Consumer<UploadInfo> updater) {
        UploadInfo uploadInfo = uploadInfoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("No upload info found by videoId: " + videoId));

        updater.accept(uploadInfo);
        return uploadInfoRepository.save(uploadInfo);
    }

}
