package com.app.playerservicejava.exception;

import com.app.playerservicejava.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorCodeHttpStatusMapper statusMapper;

    public GlobalExceptionHandler(ErrorCodeHttpStatusMapper statusMapper) {
        this.statusMapper = statusMapper;
    }

    @ExceptionHandler(PlayerBaseException.class)
    public ResponseEntity<ErrorResponse> handlePlayerException(PlayerBaseException ex) {
        log.error("message=PlayerException; errorCode={}; message={}", ex.getErrorCode(), ex.getMessage());
        HttpStatus status = statusMapper.resolve(ex.getErrorCode());
        return new ResponseEntity<>(new ErrorResponse(ex.getErrorCode(), ex.getMessage(), Instant.now()), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("message=UnexpectedException; exception={}", ex.toString());
        return new ResponseEntity<>(
                new ErrorResponse(ErrorCodes.PLAYER_SERVICE_ERROR, "An unexpected error occurred", Instant.now()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
