package com.racoonsfinds.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.purchase.PurchaseRequestDto;
import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;
import com.racoonsfinds.backend.service.PurchaseServiceImpl;
import com.racoonsfinds.backend.service.int_.PurchaseService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseServiceImpl purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseFromCart(
            @RequestBody PurchaseRequestDto req) {
        return purchaseService.purchaseFromCart(req);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getMyPurchases() {
        return purchaseService.getMyPurchases();
    }

}