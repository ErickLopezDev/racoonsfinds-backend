package com.racoonsfinds.backend.service.int_;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.payment.PaymentRequestDto;
import com.racoonsfinds.backend.dto.payment.PaymentResponseDto;
import org.springframework.http.ResponseEntity;

public interface PaymentService {

    ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(PaymentRequestDto request);
}