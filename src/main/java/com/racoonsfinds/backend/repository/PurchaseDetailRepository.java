package com.racoonsfinds.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racoonsfinds.backend.model.PurchaseDetail;

public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, Long> { }
