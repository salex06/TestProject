package salex.messenger.api;

import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import salex.messenger.service.ImageStorageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageRestController {
    private final ImageStorageService storageService;

    @GetMapping("/{filename}")
    public ResponseEntity<?> getImage(@PathVariable String filename) {
        Resource image = storageService.loadAsResource(filename);
        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String contentType = determineContentType(image);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(image);
    }

    private String determineContentType(Resource image) {
        try {
            return Files.probeContentType(image.getFile().toPath());
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}
