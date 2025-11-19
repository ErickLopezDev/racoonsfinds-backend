package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.review.ReviewRequestDto;
import com.racoonsfinds.backend.dto.review.ReviewResponseDto;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.Review;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.ProductRepository;
import com.racoonsfinds.backend.repository.ReviewRepository;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.service.int_.ReviewService;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(ReviewRequestDto request) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Check if user already reviewed this product
        boolean alreadyReviewed = reviewRepository.findByProductId(request.getProductId())
                .stream().anyMatch(r -> r.getUser().getId().equals(userId));
        if (alreadyReviewed) {
            throw new NotFoundException("Ya has reseñado este producto");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setStars(request.getStars());
        review.setComment(request.getComment());
        review.setDate(LocalDate.now());

        Review savedReview = reviewRepository.save(review);

        ReviewResponseDto response = mapToDto(savedReview);
        return ResponseUtil.created("Reseña creada exitosamente", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getReviewsByProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        List<ReviewResponseDto> response = reviews.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseUtil.ok("Reseñas obtenidas", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Double>> getAverageRating(Long productId) {
        Double average = reviewRepository.findAverageRatingByProductId(productId);
        if (average == null) average = 0.0;
        return ResponseUtil.ok("Promedio de calificación", average);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Long>> getReviewCount(Long productId) {
        Long count = reviewRepository.countByProductId(productId);
        return ResponseUtil.ok("Número de reseñas", count);
    }

    private ReviewResponseDto mapToDto(Review review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(review.getId());
        dto.setStars(review.getStars());
        dto.setComment(review.getComment());
        dto.setDate(review.getDate());
        dto.setProductId(review.getProduct().getId());
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getUsername());
        }
        return dto;
    }
}