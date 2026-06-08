package com.racoonsfinds.backend.controller;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.review.ReviewRequestDto;
import com.racoonsfinds.backend.dto.review.ReviewResponseDto;
import com.racoonsfinds.backend.service.int_.ReviewService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(@Valid @RequestBody ReviewRequestDto request) {
        return ResponseUtil.created("Reseña creada exitosamente", reviewService.createReview(request));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseUtil.ok("Reseñas obtenidas", reviewService.getReviewsByProduct(productId));
    }

    @GetMapping("/product/{productId}/average")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long productId) {
        return ResponseUtil.ok("Promedio de calificación", reviewService.getAverageRating(productId));
    }

    @GetMapping("/product/{productId}/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCount(@PathVariable Long productId) {
        return ResponseUtil.ok("Número de reseñas", reviewService.getReviewCount(productId));
    }
}
