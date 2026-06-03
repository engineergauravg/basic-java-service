package com.app.playerservicejava.model;

import java.util.List;

public record PlayersResponse(
        List<PlayerResponse> players,
        PaginationMetadata pagination
) {}
