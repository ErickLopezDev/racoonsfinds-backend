package com.racoonsfinds.backend.service.adapter;

import org.springframework.stereotype.Component;

import com.racoonsfinds.backend.repository.ReviewRepository;
import com.racoonsfinds.backend.service.port.ReviewStatsPort;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador local: accede al dominio Review via DB directa.
 * Reemplazar por HttpReviewStatsAdapter al extraer review-service.
 */
@Component
@RequiredArgsConstructor
public class LocalReviewStatsAdapter implements ReviewStatsPort {

    private final ReviewRepository reviewRepository;

    @Override
    public Double averageRating(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public long count(Long productId) {
        Long count = reviewRepository.countByProductId(productId);
        return count != null ? count : 0L;
    }
}
