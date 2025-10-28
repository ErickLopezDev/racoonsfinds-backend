package com.racoonsfinds.backend.service.int_;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;

public interface PurchaseService {
    ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseFromCart(String description);
    ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseOne(Long cartId, String description);
    ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getMyPurchases();
    ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getMySales();
}
