package com.racoonsfinds.backend.shared.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.racoonsfinds.backend.dto.ApiResponse;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message,T data) {
        return ResponseEntity.ok(new ApiResponse<>(message, true ,data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(message,true ,data));
    }

    public static ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(new ApiResponse<>(message, true, null));
    }

    public static ResponseEntity<ApiResponse<Void>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(message, true, null));
    }
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message, T data) {
        ApiResponse<T> apiResponse = new ApiResponse<>(message, false, data);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}
