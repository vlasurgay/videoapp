package videoapp.web.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import videoapp.common.model.dto.VideoDetails;
import videoapp.web.service.VideoQueryService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/videos")
public class VideoQueryController {

    private final VideoQueryService videoQueryService;

    @GetMapping(value = "/{publicId}")
    public ResponseEntity<VideoDetails> getVideoByPublicId(@PathVariable("publicId") String publicId) {
        return ResponseEntity.ok(videoQueryService.getVideoByPublicId(publicId));
    }
}
