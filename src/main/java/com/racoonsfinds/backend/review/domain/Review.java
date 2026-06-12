package com.racoonsfinds.backend.review.domain;

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
@Table(name = "reviews")
@Getter @Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stars", nullable = false)
    private Integer stars; // 1-5

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ID plano hacia catalog (sin FK JPA): cada servicio tendrá su propia BD.
    // La validación de existencia cruza el boundary via ProductCatalogPort.
    @Column(name = "product_id", nullable = false)
    private Long productId;
}