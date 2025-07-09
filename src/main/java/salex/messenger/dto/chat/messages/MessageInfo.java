package salex.messenger.dto.chat.messages;

import java.time.LocalDateTime;

public record MessageInfo(String text, LocalDateTime createdAt, String senderUsername, String receiverUsername) {}
