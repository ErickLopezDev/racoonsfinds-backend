package com.racoonsfinds.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.racoonsfinds.backend.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
        Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
        Page<Product> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

        @Query("""
        SELECT p FROM Product p
        WHERE p.user.id = :userId
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (
        LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
        Page<Product> searchProductsByUserAndText(
        @Param("userId") Long userId,
        @Param("categoryId") Long categoryId,
        @Param("search") String search,
        Pageable pageable
        );

        Page<Product> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name, Pageable pageable);
        Page<Product> findByUserId(Long userId, Pageable pageable);
        Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable
        );
        Page<Product> findByCategoryIdAndNameContainingIgnoreCaseOrCategoryIdAndDescriptionContainingIgnoreCase(
            Long categoryId1, String name,
            Long categoryId2, String description,
            Pageable pageable
    );
        @Query("""
        SELECT p
        FROM Product p
        WHERE p.user.id = :userId
        ORDER BY p.createdDate DESC
    """)
    Page<Product> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

}