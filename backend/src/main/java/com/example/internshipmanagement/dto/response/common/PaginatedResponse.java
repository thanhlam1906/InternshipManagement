package com.example.internshipmanagement.dto.response.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> items;
    private PaginationInfo pagination;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private int currentPage;
        private int pageSize;
        private int totalPages;
        private long totalItems;
    }
}

