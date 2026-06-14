package com.racoonsfinds.backend.review;

import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.catalog.port.ProductSnapshot;
import com.racoonsfinds.backend.identity.port.UserDirectoryPort;
import com.racoonsfinds.backend.identity.port.UserSnapshot;
import com.racoonsfinds.backend.review.dto.ReviewRequestDto;
import com.racoonsfinds.backend.review.service.ReviewService;
import com.racoonsfinds.backend.shared.event.ReviewStatsChangedEvent;
import com.racoonsfinds.backend.support.PostgresTestContainer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.PublishedEvents;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Verifica el contrato del módulo review en aislamiento (@ApplicationModuleTest
 * arranca solo este módulo): al crear una reseña debe publicarse el evento de
 * integración ReviewStatsChangedEvent. Los puertos hacia catalog e identity se
 * mockean — review no conoce sus implementaciones. El repositorio corre contra
 * un Postgres real (Testcontainers).
 */
@ApplicationModuleTest
class ReviewEventPublicationTest extends PostgresTestContainer {

    @Autowired
    private ReviewService reviewService;

    @MockitoBean
    private ProductCatalogPort productCatalogPort;

    @MockitoBean
    private UserDirectoryPort userDirectoryPort;

    @BeforeEach
    void authenticateUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publishesReviewStatsChangedEventWhenReviewIsCreated(PublishedEvents events) {
        Long productId = 42L;
        when(productCatalogPort.findById(productId))
                .thenReturn(new ProductSnapshot(productId, "Mate", BigDecimal.TEN, null, 7, 2L));
        when(userDirectoryPort.findById(1L))
                .thenReturn(new UserSnapshot(1L, "alice"));

        reviewService.createReview(new ReviewRequestDto(productId, 5, "Excelente"));

        var published = events.ofType(ReviewStatsChangedEvent.class);
        assertThat(published).hasSize(1);

        ReviewStatsChangedEvent event = published.iterator().next();
        assertThat(event.productId()).isEqualTo(productId);
        assertThat(event.reviewCount()).isEqualTo(1L);
        assertThat(event.averageRating()).isEqualTo(5.0);
    }
}
