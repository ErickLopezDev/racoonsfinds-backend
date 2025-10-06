package com.racoonsfinds.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.racoonsfinds.backend.model.User;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.failedAttempts = :attempts, u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateFailedAttemptsAndLastLogin(
        @Param("attempts") int attempts,
        @Param("lastLogin") LocalDateTime lastLogin,
        @Param("userId") Long userId
    );
}

