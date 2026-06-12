package com.racoonsfinds.backend.shared.event;

/**
 * Evento de integración: review lo publica cuando cambian las reseñas de un producto;
 * catalog lo consume y persiste las estadísticas (denormalización).
 * Vive en shared (contrato neutro) para no acoplar catalog -> review.
 * En microservicios se externaliza vía Modulith republicando el mismo evento a RabbitMQ/Kafka.
 */
public record ReviewStatsChangedEvent(Long productId, Double averageRating, long reviewCount) {
}
