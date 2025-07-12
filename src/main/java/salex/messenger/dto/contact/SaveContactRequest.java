package salex.messenger.dto.contact;

import jakarta.validation.constraints.NotBlank;

public record SaveContactRequest(@NotBlank String owner, @NotBlank String contact) {}
