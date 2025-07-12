package salex.messenger.dto.users;

import jakarta.validation.constraints.NotBlank;

public record UsernameResponse(@NotBlank String username) {}
