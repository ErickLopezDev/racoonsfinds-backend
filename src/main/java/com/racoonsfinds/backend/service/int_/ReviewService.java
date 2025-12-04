package com.racoonsfinds.backend.service.int_;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.review.ReviewRequestDto;
import com.racoonsfinds.backend.dto.review.ReviewResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReviewService {

    ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(ReviewRequestDto request);

    ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getReviewsByProduct(Long productId);

    ResponseEntity<ApiResponse<Double>> getAverageRating(Long productId);

    ResponseEntity<ApiResponse<Long>> getReviewCount(Long productId);
}