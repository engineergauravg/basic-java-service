package com.app.playerservicejava.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ErrorCodeHttpStatusMapper {

    private static final Map<String, HttpStatus> MAPPINGS = Map.of(
            ErrorCodes.PLAYER_NOT_FOUND,       HttpStatus.NOT_FOUND,
            ErrorCodes.PLAYER_ALREADY_EXISTS,  HttpStatus.CONFLICT,
            ErrorCodes.PLAYER_SERVICE_ERROR,   HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCodes.EXTERNAL_SERVICE_ERROR, HttpStatus.BAD_GATEWAY
    );

    public HttpStatus resolve(String errorCode) {
        return MAPPINGS.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
