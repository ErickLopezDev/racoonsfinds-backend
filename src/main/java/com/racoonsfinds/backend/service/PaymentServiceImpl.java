package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.payment.PaymentRequestDto;
import com.racoonsfinds.backend.dto.payment.PaymentResponseDto;
import com.racoonsfinds.backend.model.Purchase;
import com.racoonsfinds.backend.repository.PurchaseRepository;
import com.racoonsfinds.backend.service.int_.PaymentService;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PurchaseRepository purchaseRepository;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(PaymentRequestDto request) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new NotFoundException("Compra no encontrada"));

        if (!purchase.getUser().getId().equals(userId)) {
            throw new NotFoundException("No autorizado para procesar este pago");
        }

        if (!"PENDING".equals(purchase.getPaymentStatus())) {
            throw new NotFoundException("El pago ya ha sido procesado");
        }

        // Simulate payment processing
        boolean paymentSuccess = simulatePayment(request);

        String transactionId = UUID.randomUUID().toString();
        String status = paymentSuccess ? "COMPLETED" : "FAILED";

        purchase.setPaymentStatus(status);
        purchase.setPaymentMethod(request.getPaymentMethod());
        purchase.setTransactionId(transactionId);
        purchaseRepository.save(purchase);

        PaymentResponseDto response = new PaymentResponseDto();
        response.setPurchaseId(purchase.getId());
        response.setPaymentStatus(status);
        response.setTransactionId(transactionId);
        response.setMessage(paymentSuccess ? "Pago procesado exitosamente" : "Pago fallido");

        return ResponseUtil.ok("Pago procesado", response);
    }

    private boolean simulatePayment(PaymentRequestDto request) {
        // Simple simulation: succeed if card number is not empty and CVV is 3 digits
        return request.getCardNumber() != null && !request.getCardNumber().isEmpty() &&
               request.getCvv() != null && request.getCvv().length() == 3;
    }
}