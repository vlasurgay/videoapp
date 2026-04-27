package videoapp.worker.preparation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.dto.VideoMetadata;
import videoapp.common.model.entity.Video;
import videoapp.common.model.processing.JobPlanningContext;
import videoapp.core.service.VideoService;
import videoapp.worker.service.JobPlannerService;
import videoapp.worker.service.ProbeVideoService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static videoapp.common.Constants.BASIC_UPLOAD_KEY_REGEXP;

@Slf4j
@Component
public class VideoPreparationManager {

    private static final Pattern UPLOAD_KEY_PATTERN = Pattern.compile(BASIC_UPLOAD_KEY_REGEXP);

    private final ProbeVideoService probeVideoService;
    private final VideoService videoService;
    private final JobPlannerService jobPlannerService;

    public VideoPreparationManager(ProbeVideoService probeVideoService, VideoService videoService, JobPlannerService jobPlannerService) {
        this.probeVideoService = probeVideoService;
        this.videoService = videoService;
        this.jobPlannerService = jobPlannerService;
    }

    public void process(String originUrl) {
        String[] data = parseUploadKey(originUrl);
        String publicId = data[0];
        String fileName = data[1];

        Video video = videoService.findByPublicId(publicId);
        VideoMetadata metadata = probeVideoService.probe(video.getId(), originUrl);

        JobPlanningContext context = new JobPlanningContext(
                video.getId(),
                publicId,
                fileName,
                originUrl,
                metadata,
                video.getTargetSettings()
        );
        jobPlannerService.planJobs(context);

        log.info("Video {} is ready for processing. Jobs dispatched.", publicId);
    }

    private String[] parseUploadKey(String originKey) {
        Matcher matcher = UPLOAD_KEY_PATTERN.matcher(originKey);
        if (matcher.matches()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        throw new IllegalArgumentException("Invalid origin key format: " + originKey);
    }
}
