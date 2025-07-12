package salex.messenger.dto.chat.messages;

import jakarta.validation.constraints.NotBlank;

public record SimpleMessage(@NotBlank String text) {}
