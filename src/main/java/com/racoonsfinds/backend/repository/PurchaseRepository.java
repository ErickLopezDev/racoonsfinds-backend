package com.racoonsfinds.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racoonsfinds.backend.model.Purchase;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUserId(Long userId);
}
