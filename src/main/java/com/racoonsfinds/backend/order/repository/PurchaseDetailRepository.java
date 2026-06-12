package com.racoonsfinds.backend.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racoonsfinds.backend.order.domain.PurchaseDetail;

public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, Long> { }
