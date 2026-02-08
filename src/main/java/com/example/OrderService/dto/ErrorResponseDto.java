package com.example.OrderService.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDto(
        int statusCode,
        String message,
        LocalDateTime timestamp,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> validationErrors
) {
    public ErrorResponseDto(int statusCode, String message) {
        this(statusCode, message, LocalDateTime.now(), null);
    }

    public ErrorResponseDto(int statusCode, String message, Map<String, String> validationErrors) {
        this(statusCode, message, LocalDateTime.now(), validationErrors);
    }
}
