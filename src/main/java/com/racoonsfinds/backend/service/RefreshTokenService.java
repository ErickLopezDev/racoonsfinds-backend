package com.racoonsfinds.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.racoonsfinds.backend.model.RefreshToken;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration refreshTokenDuration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${auth.refresh-expiration:43200}") long refreshTokenDurationMinutes) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDuration = Duration.ofMinutes(refreshTokenDurationMinutes); // Ej: 30 dias = 43200
    }

        @Transactional
        public RefreshToken createRefreshToken(User user) {
            refreshTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plus(refreshTokenDuration);
            RefreshToken refreshToken = new RefreshToken(token, user, expiry);
            return refreshTokenRepository.save(refreshToken);
        }


    public boolean isValid(RefreshToken token) {
        return token != null && token.getExpiryAt().isAfter(LocalDateTime.now());
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElse(null);
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}