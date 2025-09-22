package vlsurhai.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vlsurhai.common.model.presign.S3CompletedBatch;
import vlsurhai.common.model.presign.S3PresignedBatch;
import vlsurhai.common.model.video.UploadInfo;
import vlsurhai.storage.s3.S3Repository;
import vlsurhai.web.service.VideoService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@RestController
@RequestMapping("/api/upload")
public class VideoController {

    @Deprecated
    @Autowired
    private S3Repository s3Repository;

    @Autowired
    private VideoService videoService;

    @PostMapping(value = "/init-multipart")
    public ResponseEntity<S3PresignedBatch> initMultipartUpload(@RequestBody UploadInfo uploadInfo) {
        if (isNotBlank(uploadInfo.getTotalParts(), uploadInfo.getTitle(), uploadInfo.getOriginalMetadata(), uploadInfo.getTransformRequest())){

            return ResponseEntity.ok(videoService.initiateMultipartUpload(uploadInfo));
        }
        return ResponseEntity.badRequest().body(null);
    }

    @PostMapping(value = "/complete-multipart")
    public ResponseEntity<Void> completeMultipartUpload(@RequestBody S3CompletedBatch completedBatch) {
        if (isNotBlank(completedBatch.getKey(), completedBatch.getUploadId(), completedBatch.getETags())){
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

    @Deprecated
    @PostMapping(value = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadChunk(@RequestParam("fileId") String fileId,
                                              @RequestParam("chunkNumber") int chunkNumber,
                                              @RequestParam("totalChunks") int totalChunks,
                                              @RequestPart("file") MultipartFile file) {
        if (isNotBlank(fileId, file)){
            try {
                String key = "uploads/" + fileId + "/chunk_" + chunkNumber;

                s3Repository.putObject(key, file.getBytes());

                return ResponseEntity.ok("Chunk " + chunkNumber + " uploaded");
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Failed to upload chunk");
            }
        } else return ResponseEntity.status(500).body("Wrong parameters");
    }

    private boolean isNotBlank(Object ... objects) {
        return Arrays.stream(objects).allMatch(Objects::nonNull);
    }
}
