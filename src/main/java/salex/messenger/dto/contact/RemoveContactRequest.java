package salex.messenger.dto.contact;

import jakarta.validation.constraints.NotBlank;

public record RemoveContactRequest(@NotBlank String owner, @NotBlank String contact) {}
