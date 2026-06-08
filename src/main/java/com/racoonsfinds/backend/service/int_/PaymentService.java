package com.racoonsfinds.backend.service.int_;

import com.racoonsfinds.backend.dto.payment.PaymentRequestDto;
import com.racoonsfinds.backend.dto.payment.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto processPayment(PaymentRequestDto request);
}
