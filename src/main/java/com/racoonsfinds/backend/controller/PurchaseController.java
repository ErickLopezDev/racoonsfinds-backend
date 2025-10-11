package com.racoonsfinds.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;
import com.racoonsfinds.backend.service.int_.PurchaseService;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseFromCart(
            @RequestParam(required = false) String description
    ) {
        return purchaseService.purchaseFromCart(description);
    }

    @PostMapping("/one/{cartId}")
    public ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseOne(
            @PathVariable Long cartId,
            @RequestParam(required = false) String description
    ) {
        return purchaseService.purchaseOne(cartId, description);
    }
}