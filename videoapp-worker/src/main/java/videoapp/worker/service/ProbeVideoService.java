package videoapp.worker.service;

import org.springframework.stereotype.Service;
import videoapp.common.model.dto.VideoMetadata;
import videoapp.core.service.UploadInfoService;
import videoapp.storage.api.StorageProvider;
import videoapp.worker.integration.ffprobe.FfprobeClient;

@Service
public class ProbeVideoService {

    private final StorageProvider storageProvider;
    private final FfprobeClient ffprobeClient;
    private final UploadInfoService uploadInfoService;

    public ProbeVideoService(StorageProvider storageProvider, FfprobeClient ffprobeClient, UploadInfoService uploadInfoService) {
        this.storageProvider = storageProvider;
        this.ffprobeClient = ffprobeClient;
        this.uploadInfoService = uploadInfoService;
    }

    public VideoMetadata probe(Long videoId, String originKey) {
        String originDownloadUrl = storageProvider.getObjectPresignedUrl(originKey);

        VideoMetadata videoMetadata = ffprobeClient.probe(originDownloadUrl);

        uploadInfoService.updateByVideoId(videoId, uploadInfo -> uploadInfo.setBaseMetadata(videoMetadata));

        return videoMetadata;
    }
}
