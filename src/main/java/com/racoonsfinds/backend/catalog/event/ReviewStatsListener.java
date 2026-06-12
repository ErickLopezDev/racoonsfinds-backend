package com.racoonsfinds.backend.catalog.event;

import com.racoonsfinds.backend.catalog.domain.Product;
import com.racoonsfinds.backend.catalog.repository.ProductRepository;
import com.racoonsfinds.backend.shared.event.ReviewStatsChangedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Persiste en catalog las estadísticas de reseñas calculadas por review.
 * Denormalización: las lecturas de Product quedan locales (sin cruzar el boundary).
 */
@Component
@RequiredArgsConstructor
public class ReviewStatsListener {

    private final ProductRepository productRepository;

    @ApplicationModuleListener
    public void on(ReviewStatsChangedEvent event) {
        Product product = productRepository.findById(event.productId()).orElse(null);
        if (product == null) {
            return;
        }
        product.setAverageRating(event.averageRating());
        product.setReviewCount(event.reviewCount());
        productRepository.save(product);
    }
}
