package com.racoonsfinds.backend.notification.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter @Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 45)
    private String title;

    @Column(length = 250)
    private String message;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "read")
    private Boolean read = false;

    // ID plano hacia identity (sin FK JPA): cada servicio tendrá su propia BD.
    @Column(name = "user_Id")
    private Long userId;
}
