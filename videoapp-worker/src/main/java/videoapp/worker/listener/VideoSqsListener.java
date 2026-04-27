package videoapp.worker.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.worker.preparation.VideoPreparationManager;

import java.util.List;

@Slf4j
@Component
public class VideoSqsListener {

    private final EventParser eventParser;
    private final VideoPreparationManager videoPreparationManager;

    public VideoSqsListener(EventParser eventParser, VideoPreparationManager videoPreparationManager) {
        this.eventParser = eventParser;
        this.videoPreparationManager = videoPreparationManager;
    }


    @SqsListener(value = "${aws.sqs.video-processing-queue}")
    public void onMessage(String jsonMessage) {
        try {
            List<String> uploadUrls = eventParser.parse(jsonMessage);

            if (uploadUrls.isEmpty()) {
                log.debug("No valid upload url found in message");
                return;
            }

            for (String uploadUrl : uploadUrls) {
                videoPreparationManager.process(uploadUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process sqs video event", e);
        }
    }
}