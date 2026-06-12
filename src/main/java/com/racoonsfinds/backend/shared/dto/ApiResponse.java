package com.racoonsfinds.backend.shared.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;
    private final Boolean success;

    public ApiResponse(String message, Boolean success, T data) {
        this.message = message;
        this.success = success;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
