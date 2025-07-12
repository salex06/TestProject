package salex.messenger.dto.signup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignUpResponse(String message, @NotNull Long userId, @NotBlank String username) {}
