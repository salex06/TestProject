package salex.messenger.dto.chat.messages;

import java.util.List;

public record ChatHistoryResponse(List<MessageInfo> history, Integer size) {}
