package com.racoonsfinds.backend.order.service;

import com.racoonsfinds.backend.order.dto.PaymentRequestDto;
import com.racoonsfinds.backend.order.dto.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto processPayment(PaymentRequestDto request);
}
