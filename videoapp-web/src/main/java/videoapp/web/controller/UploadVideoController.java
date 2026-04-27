package videoapp.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videoapp.common.model.dto.InitUploadRequest;
import videoapp.common.model.dto.VideoConfigDto;
import videoapp.common.model.upload.CompletedMultipartContext;
import videoapp.common.model.upload.MultipartUploadContext;
import videoapp.web.service.UploadVideoService;

import java.util.Arrays;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/api/upload")
public class UploadVideoController {

    private final UploadVideoService uploadVideoService;

    @GetMapping(value = "/config")
    public ResponseEntity<VideoConfigDto> getVideoConfig() {
        return ResponseEntity.ok(uploadVideoService.getAvailableVideoConfig());
    }


    @PostMapping(value = "/init-multipart")
    public ResponseEntity<MultipartUploadContext> initMultipartUpload(@RequestBody InitUploadRequest initUploadRequest) {
        if (isNotBlank(initUploadRequest.fileName(), initUploadRequest.title(), initUploadRequest.fileSizeBytes())){
            return ResponseEntity.ok(uploadVideoService.initiateMultipartUpload(initUploadRequest));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping(value = "/complete-multipart")
    public ResponseEntity<Void> completeMultipartUpload(@RequestBody CompletedMultipartContext completedMultipartContext) {
        if (isNotBlank(completedMultipartContext.key(), completedMultipartContext.uploadId(), completedMultipartContext.uploadedParts())){
            uploadVideoService.completeMultipartUpload(completedMultipartContext);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping(value = "/abort-multipart")
    public ResponseEntity<Void> abortMultipartUpload(@RequestParam String key,
                                                     @RequestParam String uploadId,
                                                     @RequestParam String status) {
        if (isNotBlank(key, uploadId, status)) {
            uploadVideoService.abortMultipartUpload(key, uploadId, status);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(null);
    }

    private boolean isNotBlank(Object ... objects) {
        return Arrays.stream(objects).allMatch(Objects::nonNull);
    }
}
