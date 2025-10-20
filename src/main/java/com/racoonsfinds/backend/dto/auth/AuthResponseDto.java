package com.racoonsfinds.backend.dto.auth;

import lombok.Data;

@Data
public class AuthResponseDto {
    private Long userId;
    private Integer status;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    
    public AuthResponseDto(Long userId, Integer status, String accessToken, String refreshToken) {
        this.userId = userId;
        this.status = status;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
