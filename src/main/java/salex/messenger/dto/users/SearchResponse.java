package salex.messenger.dto.users;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SearchResponse(
        @Valid List<UserProfileInfo> content,
        @NotNull Integer totalPages,
        @NotNull Long totalElements,
        @NotNull Integer currentPage,
        @NotNull Integer pageSize) {}
