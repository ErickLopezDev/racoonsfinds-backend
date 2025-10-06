package com.racoonsfinds.backend.shared.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.racoonsfinds.backend.dto.ApiResponse;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>(message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(message, data));
    }

    public static ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(new ApiResponse<>(message, null));
    }

    public static ResponseEntity<ApiResponse<Void>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(message, null));
    }
}
