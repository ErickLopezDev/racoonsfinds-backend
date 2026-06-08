package com.racoonsfinds.backend.service.int_;

import com.racoonsfinds.backend.dto.review.ReviewRequestDto;
import com.racoonsfinds.backend.dto.review.ReviewResponseDto;

import java.util.List;

public interface ReviewService {

    ReviewResponseDto createReview(ReviewRequestDto request);

    List<ReviewResponseDto> getReviewsByProduct(Long productId);

    Double getAverageRating(Long productId);

    Long getReviewCount(Long productId);
}
