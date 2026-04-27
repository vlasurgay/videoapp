package videoapp.worker.listener;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotificationRecord;

import java.util.List;

@Component
public class EventParser {

    public List<String> parse(String jsonMessage) {
        S3EventNotification event = S3EventNotification.fromJson(jsonMessage);

        return event.getRecords().stream()
                .map(S3EventNotificationRecord::getS3)
                .map(s3 -> s3.getObject().getKey())
                .toList();
    }
}
