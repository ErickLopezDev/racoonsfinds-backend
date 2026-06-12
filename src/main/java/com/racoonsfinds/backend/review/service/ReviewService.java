package com.racoonsfinds.backend.review.service;

import com.racoonsfinds.backend.review.dto.ReviewRequestDto;
import com.racoonsfinds.backend.review.dto.ReviewResponseDto;

import java.util.List;

public interface ReviewService {

    ReviewResponseDto createReview(ReviewRequestDto request);

    List<ReviewResponseDto> getReviewsByProduct(Long productId);

    Double getAverageRating(Long productId);

    Long getReviewCount(Long productId);
}
