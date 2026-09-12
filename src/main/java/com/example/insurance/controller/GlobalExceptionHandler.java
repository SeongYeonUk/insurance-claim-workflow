package com.example.insurance.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse>
    handleIllegalArgument(
            IllegalArgumentException e
    ) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiErrorResponse(
                                "BAD_REQUEST",
                                e.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse>
    handleIllegalState(
            IllegalStateException e
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiErrorResponse(
                                "INVALID_STATE",
                                e.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse>
    handleValidation(
            MethodArgumentNotValidException e
    ) {

        String message =
                e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .orElse(
                                "잘못된 요청입니다."
                        );

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiErrorResponse(
                                "VALIDATION_ERROR",
                                message,
                                LocalDateTime.now()
                        )
                );
    }
}