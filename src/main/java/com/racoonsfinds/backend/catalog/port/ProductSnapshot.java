package com.racoonsfinds.backend.catalog.port;

import java.math.BigDecimal;

/**
 * Proyección del dominio Catalog que usa el dominio Commerce.
 * En microservicios: respuesta del catalog-service via HTTP.
 */
public record ProductSnapshot(
        Long id,
        String name,
        BigDecimal price,
        String imageKey,
        Integer stock,
        Long sellerId
) {}
