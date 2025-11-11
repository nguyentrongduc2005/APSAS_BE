package com.project.apsas.dto.response;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private Pagination pagination;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Pagination {
        private int page;
        private int limit;
        private long totalItems;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrev;
    }
}
