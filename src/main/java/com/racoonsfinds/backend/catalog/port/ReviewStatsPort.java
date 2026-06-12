package com.racoonsfinds.backend.catalog.port;

/**
 * Puerto que catalog consume para resolver estadísticas de reseñas de un producto.
 * La interfaz vive en el consumidor (catalog) por inversión de dependencias;
 * el dominio review la implementa (LocalReviewStatsAdapter).
 * En microservicios: HttpReviewStatsAdapter (REST al review-service).
 */
public interface ReviewStatsPort {

    Double averageRating(Long productId);

    long count(Long productId);
}
