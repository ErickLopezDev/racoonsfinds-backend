package com.racoonsfinds.backend.review.service;

import com.racoonsfinds.backend.review.dto.ReviewRequestDto;
import com.racoonsfinds.backend.review.dto.ReviewResponseDto;
import com.racoonsfinds.backend.review.domain.Review;
import com.racoonsfinds.backend.review.repository.ReviewRepository;
import com.racoonsfinds.backend.review.service.ReviewService;
import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.identity.port.UserDirectoryPort;
import com.racoonsfinds.backend.identity.port.UserSnapshot;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductCatalogPort productCatalogPort;
    private final UserDirectoryPort userDirectoryPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto request) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        // Validates existence and crosses the catalog boundary via port (no direct repo access)
        Long resolvedProductId = productCatalogPort.findById(request.getProductId()).id();

        boolean alreadyReviewed = reviewRepository.findByProductId(request.getProductId())
                .stream().anyMatch(r -> r.getUserId().equals(userId));
        if (alreadyReviewed) {
            throw new ConflictException("Ya has reseñado este producto");
        }

        Review review = new Review();
        review.setProductId(resolvedProductId);
        review.setUserId(userId);
        review.setStars(request.getStars());
        review.setComment(request.getComment());
        review.setDate(LocalDate.now());

        Review saved = reviewRepository.save(review);
        ReviewResponseDto response = ReviewMapper.map(saved, ReviewResponseDto.class);
        response.setUserName(userDirectoryPort.findById(userId).username());

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
        List<Review> reviews = reviewRepository.findByProductId(productId);

        List<Long> userIds = reviews.stream()
                .map(Review::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> userNames = userDirectoryPort.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(UserSnapshot::id, UserSnapshot::username));

        return reviews.stream()
                .map(r -> {
                    ReviewResponseDto dto = ReviewMapper.map(r, ReviewResponseDto.class);
                    dto.setUserName(userNames.get(r.getUserId()));
                    return dto;
                })
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
