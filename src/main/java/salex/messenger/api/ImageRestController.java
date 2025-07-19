package salex.messenger.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import salex.messenger.service.ImageStorageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageRestController {
    private final ImageStorageService storageService;

    @GetMapping("/users/{filename}")
    public ResponseEntity<?> getUserPhoto(@PathVariable String filename) {
        return getResource(filename, storageService.getUserPhotoDir());
    }

    @GetMapping("/messages/{filename}")
    public ResponseEntity<?> getMessageImage(@PathVariable String filename) {
        return getResource(filename, storageService.getMessageMediaDir());
    }

    private ResponseEntity<?> getResource(String filename, Path dir) {
        Resource image = storageService.loadAsResource(filename, dir);
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

    @PostMapping("/messages")
    public ResponseEntity<?> uploadMessageMedia(@ModelAttribute MultipartFile image, Principal principal) {
        return uploadResource(image, principal, storageService.getMessageMediaDir());
    }

    private ResponseEntity<?> uploadResource(MultipartFile file, Principal principal, Path destDir) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String filePath =
                storageService.store(file, ImageStorageService.generateFilename(principal.getName(), file), destDir);

        return ResponseEntity.ok(filePath);
    }
}
