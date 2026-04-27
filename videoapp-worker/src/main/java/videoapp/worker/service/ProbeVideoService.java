package videoapp.worker.processing.impl;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import videoapp.common.model.dto.VideoMetadataDto;
import videoapp.common.model.processing.VideoProcessingContext;
import videoapp.core.service.s.VideoMetadataService;
import videoapp.storage.s3.S3PresignedUrlProvider;
import videoapp.worker.ffmpeg.FfprobeExecutor;

import videoapp.worker.ffmpeg.FfprobeParser;
import videoapp.worker.processing.AbstractProcessor;

@Order(2)
@Component
public class ProbeVideoProcessor implements AbstractProcessor {

    private final S3PresignedUrlProvider presignedUrlProvider;
    private final FfprobeExecutor ffprobeExecutor;
    private final FfprobeParser ffprobeParser;
    private final VideoMetadataService videoMetadataService;

    public ProbeVideoProcessor(S3PresignedUrlProvider presignedUrlProvider, FfprobeExecutor ffprobeExecutor,
                               FfprobeParser ffprobeParser, VideoMetadataService videoMetadataService) {
        this.presignedUrlProvider = presignedUrlProvider;
        this.ffprobeExecutor = ffprobeExecutor;
        this.ffprobeParser = ffprobeParser;
        this.videoMetadataService = videoMetadataService;
    }

    @Override
    public boolean shouldExecute(VideoProcessingContext context) {
        return context.getVideoMetadata() == null;
    }

    @Override
    public void execute(VideoProcessingContext context) {
        String s3Key = context.getOriginS3Key();
        String s3OriginDownloadUrl = presignedUrlProvider.presignGetObject(s3Key).url().toString();

        String jsonMetadata = ffprobeExecutor.execute(s3OriginDownloadUrl);
        VideoMetadataDto videoMetadataDto = ffprobeParser.parse(jsonMetadata, context);
        context.setVideoMetadata(videoMetadataDto);

        videoMetadataService.upsertMetadata(videoMetadataDto);
    }

    private void defineTargetQualityProfiles(VideoProcessingContext context) {
        VideoMetadataDto metadata = context.getVideoMetadata();
        if (metadata != null) {
            context.setTargetQualityProfiles(
                    VideoQualityProfile.getProfilesForHeight(metadata.height())
            );
        }
    }
}
