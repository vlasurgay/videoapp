package videoapp.worker.listener;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.eventnotifications.s3.model.S3;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotificationRecord;
import software.amazon.awssdk.eventnotifications.s3.model.S3Object;

import java.util.List;
import java.util.Objects;

import static videoapp.common.Constants.COMPLETE_MULTIPART_ACTION;

@Component
public class EventParser {

    public List<String> parse(String jsonMessage) {
        S3EventNotification event = S3EventNotification.fromJson(jsonMessage);

        if (event == null || event.getRecords() == null) {
            return List.of();
        }

        return event.getRecords().stream()
                .filter(Objects::nonNull)
                .filter(record -> COMPLETE_MULTIPART_ACTION.equals(record.getEventName()))
                .map(S3EventNotificationRecord::getS3)
                .filter(Objects::nonNull)
                .map(S3::getObject)
                .filter(Objects::nonNull)
                .map(S3Object::getUrlDecodedKey)
                .toList();
    }
}
