package com.app.playerservicejava.model;

public record PaginationMetadata(
        int size,
        boolean hasMore,
        String nextCursor,  // populated in cursor mode
        Integer nextPage    // populated in offset mode
) {}
