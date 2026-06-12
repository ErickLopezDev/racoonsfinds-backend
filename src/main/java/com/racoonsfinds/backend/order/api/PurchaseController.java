package com.racoonsfinds.backend.order.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.shared.dto.ApiResponse;
import com.racoonsfinds.backend.order.dto.PurchaseResponseDto;
import com.racoonsfinds.backend.order.service.PurchaseService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseFromCart(
            @RequestParam(required = false) String description) {
        return ResponseUtil.created("Compra realizada con éxito", purchaseService.purchaseFromCart(description));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getMyPurchases() {
        return ResponseUtil.ok("Listado de compras", purchaseService.getMyPurchases());
    }
}
