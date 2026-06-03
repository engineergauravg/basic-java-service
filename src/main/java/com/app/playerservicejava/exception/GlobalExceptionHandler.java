package com.app.playerservicejava.exception;

import com.app.playerservicejava.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("message=ValidationFailed; errors={}", message);
        return new ResponseEntity<>(
                new ErrorResponse(ErrorCodes.VALIDATION_ERROR, message, Instant.now()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        log.warn("message=BadRequest; exception={}", ex.toString());
        return new ResponseEntity<>(
                new ErrorResponse(ErrorCodes.VALIDATION_ERROR, ex.getMessage(), Instant.now()),
                HttpStatus.BAD_REQUEST
        );
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
