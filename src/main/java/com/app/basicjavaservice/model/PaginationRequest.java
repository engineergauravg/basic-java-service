package com.app.basicjavaservice.model;

public record PaginationRequest(
        Integer page,
        String after,
        int size
) {
    public PaginationRequest {
        if (size <= 0) size = 20;
    }

    public boolean isCursorBased() {
        return after != null && !after.isBlank();
    }
}
