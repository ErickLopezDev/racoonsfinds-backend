package com.racoonsfinds.backend.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racoonsfinds.backend.identity.domain.RefreshToken;
import com.racoonsfinds.backend.identity.domain.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}