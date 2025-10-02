package com.racoonsfinds.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 45, unique = true)
  private String email;

  @Column(nullable = false, length = 70)
  private String password;

  @Column(nullable = false)
  private LocalDate birthDate;
  
  private String imageUrl;

  private Boolean verified = false;

  private String verificationCode;     

  private LocalDateTime codeExpiry = LocalDateTime.now().plusMinutes(15); // 15 min    

  private Integer failedAttempts = 0;

  private LocalDateTime lastLogin;

  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime createdAt = LocalDateTime.now();

}
