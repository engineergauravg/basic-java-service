package com.app.basicjavaservice.model;

import java.util.List;

public record PlayersResponse(
        List<PlayerResponse> players,
        PaginationMetadata pagination
) {}
