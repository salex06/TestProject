package salex.messenger.dto.users;

import java.util.List;

public record SearchResponse(
        List<UserProfileInfo> content, Integer totalPages, Long totalElements, Integer currentPage, Integer pageSize) {}
