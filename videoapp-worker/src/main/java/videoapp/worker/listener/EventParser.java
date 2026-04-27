package videoapp.worker.eventlistener;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.eventnotifications.s3.model.S3;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotificationRecord;
import videoapp.common.model.processing.VideoProcessingContext;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static videoapp.common.Constants.BASIC_UPLOAD_S3_KEY_REGEXP;

@Component
public class EventParser {

    private static final Pattern S3_KEY_PATTERN = Pattern.compile(BASIC_UPLOAD_S3_KEY_REGEXP);

    public List<VideoProcessingContext> parse(String jsonMessage) {
        S3EventNotification event = S3EventNotification.fromJson(jsonMessage);

        return event.getRecords().stream()
                .map(S3EventNotificationRecord::getS3)
                .map(this::createContextFromS3)
                .filter(Objects::nonNull)
                .toList();
    }

    private VideoProcessingContext createContextFromS3(S3 s3Record) {
        Matcher matcher = S3_KEY_PATTERN.matcher(s3Record.getObject().getKey());
        if (matcher.matches()) {
            return new VideoProcessingContext(matcher.group(1), s3Record.getObject().getKey(), s3Record.getBucket().getName());
        }
        return null;
    }
}
