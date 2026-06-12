package com.racoonsfinds.backend.review.port;

/**
 * Puerto hacia el dominio Review (estadísticas de producto).
 * Implementación actual: LocalReviewStatsAdapter (DB directa).
 * En microservicios: HttpReviewStatsAdapter (REST al review-service).
 */
public interface ReviewStatsPort {

    Double averageRating(Long productId);

    long count(Long productId);
}
