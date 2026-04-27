package videoapp.worker.eventlistener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import videoapp.common.model.processing.VideoProcessingContext;
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
            List<VideoProcessingContext> contexts = eventParser.parse(jsonMessage);

            if (contexts.isEmpty()) {
                log.debug("No valid video processing contexts found in message");
                return;
            }

            for (VideoProcessingContext context : contexts) {
                videoPreparationManager.process(context);
            }
        } catch (Exception e) {
            log.error("Critical error during SQS message processing");
            throw new RuntimeException("Failed to process sqs video event", e);
        }
    }
}