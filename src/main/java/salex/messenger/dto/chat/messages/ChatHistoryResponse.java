package salex.messenger.dto.chat.messages;

import jakarta.validation.Valid;
import java.util.List;

public record ChatHistoryResponse(@Valid List<MessageInfo> history, Integer size) {}
