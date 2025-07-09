package salex.messenger.dto.chat.user;

import java.util.List;

public record ChatPartnersResponse(List<ChatPartnerInfo> partners, Integer size) {}
