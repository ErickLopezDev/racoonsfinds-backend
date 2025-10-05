package com.racoonsfinds.backend.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthResponseDto{
    public AuthResponseDto(Long id, String access, String token) {
        //TODO Auto-generated constructor stub
    }
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
