package salex.messenger.dto.signin;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(@NotBlank String username, @NotBlank String password) {}
