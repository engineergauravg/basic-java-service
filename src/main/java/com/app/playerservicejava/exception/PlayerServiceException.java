package com.app.playerservicejava.exception;

public class PlayerServiceException extends PlayerBaseException {
    public PlayerServiceException(String message, Throwable cause) {
        super(ErrorCodes.PLAYER_SERVICE_ERROR, message, cause);
    }
}
