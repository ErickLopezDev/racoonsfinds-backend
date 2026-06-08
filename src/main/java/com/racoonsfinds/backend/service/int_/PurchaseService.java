package com.racoonsfinds.backend.service.int_;

import java.util.List;

import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;

public interface PurchaseService {
    PurchaseResponseDto purchaseFromCart(String description);
    List<PurchaseResponseDto> getMyPurchases();
    List<PurchaseResponseDto> getMySales();
}
