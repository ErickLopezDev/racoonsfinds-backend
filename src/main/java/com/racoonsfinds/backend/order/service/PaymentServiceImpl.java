package com.racoonsfinds.backend.order.service;

import com.racoonsfinds.backend.order.dto.PaymentRequestDto;
import com.racoonsfinds.backend.order.dto.PaymentResponseDto;
import com.racoonsfinds.backend.order.domain.Purchase;
import com.racoonsfinds.backend.order.repository.PurchaseRepository;
import com.racoonsfinds.backend.order.service.PaymentService;
import com.racoonsfinds.backend.shared.exception.BadRequestException;
import com.racoonsfinds.backend.shared.exception.ForbiddenException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.exception.UnauthorizedException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PurchaseRepository purchaseRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new UnauthorizedException("Usuario no autenticado");

        Purchase purchase = purchaseRepository.findById(request.getPurchaseId())
                .orElseThrow(() -> new NotFoundException("Compra no encontrada"));

        if (!purchase.getUser().getId().equals(userId)) {
            throw new ForbiddenException("No autorizado para procesar este pago");
        }

        if (!"PENDING".equals(purchase.getPaymentStatus())) {
            throw new BadRequestException("El pago ya ha sido procesado");
        }

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
        return response;
    }

    private boolean simulatePayment(PaymentRequestDto request) {
        return request.getCardNumber() != null && !request.getCardNumber().isEmpty() &&
               request.getCvv() != null && request.getCvv().length() == 3;
    }
}
