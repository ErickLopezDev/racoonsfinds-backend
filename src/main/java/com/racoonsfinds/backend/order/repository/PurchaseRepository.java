package com.racoonsfinds.backend.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.racoonsfinds.backend.order.domain.Purchase;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserId(Long userId);
    // Ventas de productos del usuario autenticado (usa join con details → product)
    @Query("""
        SELECT DISTINCT p FROM Purchase p
        JOIN FETCH p.purchaseDetails d
        JOIN FETCH d.product prod
        WHERE prod.user.id = :sellerId
        """)
    List<Purchase> findSalesBySellerId(@Param("sellerId") Long sellerId);
}
