package videoapp.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videoapp.common.model.jpa.UploadInfo;
import videoapp.common.model.presign.S3CompletedBatch;
import videoapp.common.model.presign.S3PresignedBatch;
import videoapp.web.service.VideoService;

import java.util.Arrays;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/api/upload")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(value = "/init-multipart")
    public ResponseEntity<S3PresignedBatch> initMultipartUpload(@RequestBody UploadInfo uploadInfo) {
        if (isNotBlank(uploadInfo.getTotalParts(), uploadInfo.getTitle(), uploadInfo.getOriginalMetadata(), uploadInfo.getTransformRequest())){

            return ResponseEntity.ok(videoService.initiateMultipartUpload(uploadInfo));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping(value = "/complete-multipart")
    public ResponseEntity<Void> completeMultipartUpload(@RequestBody S3CompletedBatch completedBatch) {
        if (isNotBlank(completedBatch.key(), completedBatch.uploadId(), completedBatch.eTags())){
            return videoService.completeMultipartUpload(completedBatch);
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping(value = "/abort-multipart")
    public ResponseEntity<Void> abortMultipartUpload(@RequestParam String key,
                                                     @RequestParam String uploadId,
                                                     @RequestParam String status) {
        if (isNotBlank(key, uploadId)) {
            return videoService.abortMultipartUpload(key, uploadId, status);
        }
        return ResponseEntity.badRequest().body(null);
    }

    private boolean isNotBlank(Object ... objects) {
        return Arrays.stream(objects).allMatch(Objects::nonNull);
    }
}
