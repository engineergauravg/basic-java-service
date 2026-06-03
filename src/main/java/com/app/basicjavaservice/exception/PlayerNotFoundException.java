package com.app.basicjavaservice.exception;

public class PlayerNotFoundException extends PlayerBaseException {
    public PlayerNotFoundException(String playerId) {
        super(ErrorCodes.PLAYER_NOT_FOUND, "Player not found: " + playerId);
    }
}
