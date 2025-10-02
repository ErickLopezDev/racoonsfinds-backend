package com.racoonsfinds.backend.dto.auth.login;

public record OAuth2RequestDto(
    String provider,    // "google"
    String providerId,  // id del usuario en Google
    String email,
    String name,
    String imageUrl
) {}
