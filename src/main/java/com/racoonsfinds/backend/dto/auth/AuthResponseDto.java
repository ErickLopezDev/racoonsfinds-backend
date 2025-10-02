package com.racoonsfinds.backend.dto.auth;

public record AuthResponseDto(
    String accessToken,
    String refreshToken
) {}
