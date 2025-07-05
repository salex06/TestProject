package salex.messenger.dto.users;

import java.util.List;

public record SearchResponse(
        List<UserInfo> content, Integer totalPages, Long totalElements, Integer currentPage, Integer pageSize) {}
