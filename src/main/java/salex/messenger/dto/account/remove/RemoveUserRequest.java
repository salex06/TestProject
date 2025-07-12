package salex.messenger.dto.account.remove;

import jakarta.validation.constraints.NotBlank;

public record RemoveUserRequest(@NotBlank String username) {}
