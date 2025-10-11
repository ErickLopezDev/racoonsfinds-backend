package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.purchase.PurchaseDetailResponseDto;
import com.racoonsfinds.backend.dto.purchase.PurchaseResponseDto;
import com.racoonsfinds.backend.model.*;
import com.racoonsfinds.backend.repository.*;
import com.racoonsfinds.backend.service.int_.NotificationService;
import com.racoonsfinds.backend.service.int_.PurchaseService;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
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
    private final NotificationService notificationService;

    // Comprar todo el carrito
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseFromCart(String description) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) throw new NotFoundException("El carrito está vacío");

        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Purchase purchase = new Purchase();
        purchase.setDate(LocalDate.now());
        purchase.setMonto(total);
        purchase.setDescription(description != null ? description : "Compra desde carrito");
        purchase.setUser(new User() {{ setId(userId); }});

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

        notificationService.createNotification(
                userId,
                "Compra realizada",
                "Tu compra #" + savedPurchase.getId() + " fue procesada con éxito."
        );

        cartRepository.deleteByUserId(userId);

        PurchaseResponseDto responseDto = mapToDto(savedPurchase);
        return ResponseUtil.created("Compra individual realizada con éxito", responseDto);
    }

    // Comprar un solo ítem del carrito
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<PurchaseResponseDto>> purchaseOne(Long cartId, String description) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        Cart cartItem = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Ítem de carrito no encontrado"));

        if (!cartItem.getUser().getId().equals(userId))
            throw new NotFoundException("No puedes comprar un ítem que no es tuyo");

        BigDecimal total = cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getAmount()));

        Purchase purchase = new Purchase();
        purchase.setDate(LocalDate.now());
        purchase.setMonto(total);
        purchase.setDescription(description != null ? description : "Compra de un producto");
        purchase.setUser(new User() {{ setId(userId); }});

        Purchase savedPurchase = purchaseRepository.save(purchase);

        PurchaseDetail detail = new PurchaseDetail();
        detail.setPurchase(savedPurchase);
        detail.setProduct(cartItem.getProduct());
        detail.setMonto(cartItem.getProduct().getPrice());
        detail.setAmount(cartItem.getAmount());

        purchaseDetailRepository.save(detail);

        savedPurchase.setPurchaseDetails(List.of(detail));

        notificationService.createNotification(
                userId,
                "Compra individual realizada",
                "Has comprado el producto " + cartItem.getProduct().getName()
        );

        cartRepository.delete(cartItem);

        PurchaseResponseDto responseDto = mapToDto(savedPurchase);
        return ResponseUtil.created("Compra individual realizada con éxito", responseDto);
    }

    // === PRIVATE MAPPER ===
    private PurchaseResponseDto mapToDto(Purchase purchase) {
        PurchaseResponseDto dto = new PurchaseResponseDto();

        dto.setId(purchase.getId());
        dto.setDate(purchase.getDate());
        dto.setMonto(purchase.getMonto());
        dto.setDescription(purchase.getDescription());

        // === USER INFO ===
        if (purchase.getUser() != null) {
            dto.setUserId(purchase.getUser().getId());
        }

        // === DETAILS ===
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
