package com.racoonsfinds.backend.catalog.port;

import java.util.List;

/**
 * Puerto hacia el dominio Catalog.
 * Implementación actual: LocalProductCatalogAdapter (DB directa).
 * En microservicios: HttpProductCatalogAdapter (REST al catalog-service).
 */
public interface ProductCatalogPort {

    ProductSnapshot findById(Long productId);

    List<ProductSnapshot> findAllByIds(List<Long> productIds);

    void decrementStock(Long productId, int amount);
}
