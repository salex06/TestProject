package salex.messenger.dto.chat.messages;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

public record MessageInfo(
        @NotBlank String text,
        @PastOrPresent LocalDateTime createdAt,
        @NotBlank String senderUsername,
        @NotBlank String receiverUsername) {}
