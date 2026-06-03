package com.app.basicjavaservice.model;

import jakarta.validation.constraints.NotBlank;

public record UpdatePlayerRequest(
        @NotBlank(message = "firstName is required")
        String firstName,
        @NotBlank(message = "lastName is required")
        String lastName,
        String givenName,
        String birthYear,
        String birthMonth,
        String birthDay,
        String birthCountry,
        String birthState,
        String birthCity,
        String deathYear,
        String deathMonth,
        String deathDay,
        String deathCountry,
        String deathState,
        String deathCity,
        String weight,
        String height,
        String bats,
        String throwStats,
        String debut,
        String finalGame,
        String retroId,
        String bbrefId
) {}
