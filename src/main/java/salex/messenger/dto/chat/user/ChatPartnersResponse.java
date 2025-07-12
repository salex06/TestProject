package salex.messenger.dto.chat.user;

import jakarta.validation.Valid;
import java.util.List;

public record ChatPartnersResponse(@Valid List<ChatPartnerInfo> partners, Integer size) {}
