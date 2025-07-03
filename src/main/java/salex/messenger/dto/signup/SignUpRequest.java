package salex.messenger.dto.signup;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record SignUpRequest(
        @NotBlank String username,
        @NotBlank String password,
        String name,
        String surname,
        String about,
        MultipartFile photo) {}
