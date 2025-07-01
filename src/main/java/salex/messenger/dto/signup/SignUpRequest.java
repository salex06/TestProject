package salex.messenger.dto.signup;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(@NotBlank String username, @NotBlank String password) {}
