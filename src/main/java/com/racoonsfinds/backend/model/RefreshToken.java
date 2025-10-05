package com.racoonsfinds.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
public class RefreshToken {
    
    public RefreshToken(String token2, User user2, LocalDateTime expiry) {
        //TODO Auto-generated constructor stub
    }

    @Id
    @Column(length = 36)
    private String token; // UUID string

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt;

    private LocalDateTime expiryAt;
}
