package com.racoonsfinds.backend.controller;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.payment.PaymentRequestDto;
import com.racoonsfinds.backend.dto.payment.PaymentResponseDto;
import com.racoonsfinds.backend.service.int_.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(@Valid @RequestBody PaymentRequestDto request) {
        return paymentService.processPayment(request);
    }
}