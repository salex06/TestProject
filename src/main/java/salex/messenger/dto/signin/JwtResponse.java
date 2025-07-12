package salex.messenger.dto.signin;

import jakarta.validation.constraints.NotBlank;

public record JwtResponse(@NotBlank String token) {}
