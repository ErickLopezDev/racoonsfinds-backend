package com.racoonsfinds.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;
import com.racoonsfinds.backend.service.PurchaseServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/sales")
@RequiredArgsConstructor
public class SalesController {

  private final PurchaseServiceImpl purchaseService;

  @GetMapping("/")
  public ResponseEntity<ApiResponse<List<PurchaseResponseDto>>> getSalesByUser() {
        return purchaseService.getMySales();
    }
}
