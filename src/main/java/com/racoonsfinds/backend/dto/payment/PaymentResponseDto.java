package com.racoonsfinds.backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private Long purchaseId;
    private String paymentStatus;
    private String transactionId;
    private String message;
}