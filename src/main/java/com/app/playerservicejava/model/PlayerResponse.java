package com.app.playerservicejava.model;

public record PlayerResponse(
        String playerId,
        String firstName,
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
