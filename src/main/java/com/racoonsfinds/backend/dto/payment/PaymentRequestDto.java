package com.racoonsfinds.backend.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "Purchase ID is required")
    private Long purchaseId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g., "CREDIT_CARD", "PAYPAL"

    // Simulated card details
    private String cardNumber;
    private String expiryDate;
    private String cvv;
}