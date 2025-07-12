package salex.messenger.dto.contact;

import jakarta.validation.constraints.NotBlank;

public record ContactInfo(@NotBlank String username, String name, String surname, String photoPath) {}
