package salex.messenger.dto.users;

import jakarta.validation.constraints.NotBlank;

public record AccountInfo(@NotBlank String username, String name, String surname, String photoPath, String about) {}
