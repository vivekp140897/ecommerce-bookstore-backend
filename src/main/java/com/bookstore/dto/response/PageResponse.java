package com.bookstore.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        PaginationMeta meta
) {
    public record PaginationMeta(int page, int pageSize, long totalItems, int totalPages) {}
}
