package com.racoonsfinds.backend.review.service;

import com.racoonsfinds.backend.review.dto.ReviewRequestDto;
import com.racoonsfinds.backend.review.dto.ReviewResponseDto;
import com.racoonsfinds.backend.review.domain.Review;
import com.racoonsfinds.backend.identity.domain.User;
import com.racoonsfinds.backend.review.repository.ReviewRepository;
import com.racoonsfinds.backend.review.service.ReviewService;
import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.shared.exception.ConflictException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.review.mapper.ReviewMapper;
import com.racoonsfinds.backend.shared.event.ReviewStatsChangedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductCatalogPort productCatalogPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto request) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        // Validates existence and crosses the catalog boundary via port (no direct repo access)
        Long resolvedProductId = productCatalogPort.findById(request.getProductId()).id();

        // Usuario garantizado por JWT — proxy JPA para la FK sin query adicional
        User user = new User();
        user.setId(userId);

        boolean alreadyReviewed = reviewRepository.findByProductId(request.getProductId())
                .stream().anyMatch(r -> r.getUser().getId().equals(userId));
        if (alreadyReviewed) {
            throw new ConflictException("Ya has reseñado este producto");
        }

        Review review = new Review();
        review.setProductId(resolvedProductId);
        review.setUser(user);
        review.setStars(request.getStars());
        review.setComment(request.getComment());
        review.setDate(LocalDate.now());

        ReviewResponseDto response = ReviewMapper.map(reviewRepository.save(review), ReviewResponseDto.class);

        publishStatsChanged(request.getProductId());

        return response;
    }

    private void publishStatsChanged(Long productId) {
        Double average = reviewRepository.findAverageRatingByProductId(productId);
        Long count = reviewRepository.countByProductId(productId);
        eventPublisher.publishEvent(new ReviewStatsChangedEvent(
                productId,
                average != null ? average : 0.0,
                count != null ? count : 0L));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(r -> ReviewMapper.map(r, ReviewResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        Double average = reviewRepository.findAverageRatingByProductId(productId);
        return average != null ? average : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }
}
