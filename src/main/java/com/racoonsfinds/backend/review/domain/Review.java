package com.racoonsfinds.backend.review.domain;

import com.racoonsfinds.backend.identity.domain.User;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ID plano hacia catalog (sin FK JPA): cada servicio tendrá su propia BD.
    // La validación de existencia cruza el boundary via ProductCatalogPort.
    @Column(name = "product_id", nullable = false)
    private Long productId;
}