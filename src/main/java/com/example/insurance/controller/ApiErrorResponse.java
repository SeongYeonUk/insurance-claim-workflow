package com.example.insurance.controller;

import java.time.LocalDateTime;

public record ApiErrorResponse(

        String error,
        String message,
        LocalDateTime timestamp
) {
}