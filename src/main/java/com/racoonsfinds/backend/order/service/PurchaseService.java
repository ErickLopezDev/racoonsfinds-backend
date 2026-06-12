package com.racoonsfinds.backend.order.service;

import java.util.List;

import com.racoonsfinds.backend.order.dto.PurchaseResponseDto;

public interface PurchaseService {
    PurchaseResponseDto purchaseFromCart(String description);
    List<PurchaseResponseDto> getMyPurchases();
    List<PurchaseResponseDto> getMySales();
}
