package salex.messenger.dto.users;

import jakarta.validation.constraints.NotBlank;

public record UserProfileInfo(@NotBlank String username, String name, String surname, String about, String photoPath) {}
