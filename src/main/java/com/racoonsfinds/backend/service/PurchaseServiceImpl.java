package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.purchase.PurchaseDetailResponseDto;
import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;
import com.racoonsfinds.backend.model.*;
import com.racoonsfinds.backend.repository.*;
import com.racoonsfinds.backend.service.int_.NotificationService;
import com.racoonsfinds.backend.service.int_.PurchaseService;
import com.racoonsfinds.backend.shared.exception.BadRequestException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.exception.UnauthorizedException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final CartRepository cartRepository;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseDetailRepository purchaseDetailRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PurchaseResponseDto purchaseFromCart(String description) {
        Long buyerId = AuthUtil.getAuthenticatedUserId();
        if (buyerId == null) throw new UnauthorizedException("Usuario no autenticado");

        List<Cart> cartItems = cartRepository.findByUserId(buyerId);
        if (cartItems.isEmpty()) throw new BadRequestException("El carrito está vacío");

        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Purchase purchase = new Purchase();
        purchase.setDate(LocalDate.now());
        purchase.setMonto(total);
        purchase.setDescription(description != null ? description : "Compra desde carrito");
        purchase.setPaymentStatus("PENDING");
        User buyer = new User();
        buyer.setId(buyerId);
        purchase.setUser(buyer);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        List<PurchaseDetail> details = cartItems.stream().map(item -> {
            PurchaseDetail d = new PurchaseDetail();
            d.setPurchase(savedPurchase);
            d.setProduct(item.getProduct());
            d.setMonto(item.getProduct().getPrice());
            d.setAmount(item.getAmount());
            return d;
        }).toList();

        purchaseDetailRepository.saveAll(details);
        savedPurchase.setPurchaseDetails(details);

        // Descontar stock de cada producto comprado
        details.forEach(detail -> {
            Product product = detail.getProduct();
            int newStock = product.getStock() - detail.getAmount();
            if (newStock < 0) throw new BadRequestException(
                    "Stock insuficiente para el producto: " + product.getName());
            product.setStock(newStock);
            productRepository.save(product);
        });

        notificationService.createNotification(
                buyerId,
                "Compra realizada",
                "Tu compra #" + savedPurchase.getId() + " fue procesada con éxito."
        );

        details.forEach(detail -> {
            Long sellerId = detail.getProduct().getUser().getId();
            if (!sellerId.equals(buyerId)) {
                notificationService.createNotification(
                        sellerId,
                        "Producto vendido",
                        "Tu producto '" + detail.getProduct().getName() +
                        "' ha sido vendido en la compra #" + savedPurchase.getId() + "."
                );
            }
        });

        cartRepository.deleteByUserId(buyerId);

        return mapToDto(savedPurchase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getMyPurchases() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new UnauthorizedException("Usuario no autenticado");

        return purchaseRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getMySales() {
        Long sellerId = AuthUtil.getAuthenticatedUserId();
        if (sellerId == null) throw new UnauthorizedException("Usuario no autenticado");

        return purchaseRepository.findSalesBySellerId(sellerId).stream()
                .map(this::mapToDto)
                .toList();
    }

    private PurchaseResponseDto mapToDto(Purchase purchase) {
        PurchaseResponseDto dto = new PurchaseResponseDto();
        dto.setId(purchase.getId());
        dto.setDate(purchase.getDate());
        dto.setMonto(purchase.getMonto());
        dto.setDescription(purchase.getDescription());
        dto.setPaymentStatus(purchase.getPaymentStatus());
        dto.setPaymentMethod(purchase.getPaymentMethod());
        dto.setTransactionId(purchase.getTransactionId());

        if (purchase.getUser() != null) {
            dto.setUserId(purchase.getUser().getId());
        }

        if (purchase.getPurchaseDetails() != null && !purchase.getPurchaseDetails().isEmpty()) {
            List<PurchaseDetailResponseDto> detailDtos = purchase.getPurchaseDetails().stream()
                .map(detail -> {
                    PurchaseDetailResponseDto d = new PurchaseDetailResponseDto();
                    d.setId(detail.getId());
                    d.setAmount(detail.getAmount());
                    d.setMonto(detail.getMonto());
                    if (detail.getProduct() != null) {
                        d.setProductId(detail.getProduct().getId());
                        d.setProductName(detail.getProduct().getName());
                    }
                    return d;
                })
                .toList();
            dto.setDetails(detailDtos);
        }

        return dto;
    }
}
