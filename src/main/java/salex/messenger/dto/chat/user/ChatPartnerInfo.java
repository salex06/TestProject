package salex.messenger.dto.chat.user;

import jakarta.validation.constraints.NotBlank;

public record ChatPartnerInfo(@NotBlank String username, String photoPath) {}
