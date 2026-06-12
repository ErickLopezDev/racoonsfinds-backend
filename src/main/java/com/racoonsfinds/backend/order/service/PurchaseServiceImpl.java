package com.racoonsfinds.backend.order.service;

import com.racoonsfinds.backend.order.dto.PurchaseDetailResponseDto;
import com.racoonsfinds.backend.order.dto.PurchaseResponseDto;
import com.racoonsfinds.backend.catalog.domain.Product;
import com.racoonsfinds.backend.order.domain.Purchase;
import com.racoonsfinds.backend.order.domain.PurchaseDetail;
import com.racoonsfinds.backend.identity.domain.User;
import com.racoonsfinds.backend.order.repository.PurchaseDetailRepository;
import com.racoonsfinds.backend.order.repository.PurchaseRepository;
import com.racoonsfinds.backend.order.service.PurchaseService;
import com.racoonsfinds.backend.cart.port.CartItemSnapshot;
import com.racoonsfinds.backend.cart.port.CartPort;
import com.racoonsfinds.backend.notification.port.NotificationPort;
import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.catalog.port.ProductSnapshot;
import com.racoonsfinds.backend.shared.exception.BadRequestException;
import com.racoonsfinds.backend.shared.exception.UnauthorizedException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final CartPort cartPort;
    private final PurchaseRepository purchaseRepository;
    private final PurchaseDetailRepository purchaseDetailRepository;
    private final ProductCatalogPort productCatalogPort;
    private final NotificationPort notificationPort;

    @Override
    @Transactional
    public PurchaseResponseDto purchaseFromCart(String description) {
        Long buyerId = AuthUtil.getAuthenticatedUserId();
        if (buyerId == null) throw new UnauthorizedException("Usuario no autenticado");

        List<CartItemSnapshot> cartItems = cartPort.itemsOf(buyerId);
        if (cartItems.isEmpty()) throw new BadRequestException("El carrito está vacío");

        // Carga todos los snapshots en una sola llamada (1 query en monolito, 1 HTTP call en microservicio)
        List<Long> productIds = cartItems.stream().map(CartItemSnapshot::productId).toList();
        Map<Long, ProductSnapshot> snapshots = productCatalogPort.findAllByIds(productIds)
                .stream().collect(Collectors.toMap(ProductSnapshot::id, s -> s));

        BigDecimal total = cartItems.stream()
                .map(c -> snapshots.get(c.productId()).price()
                        .multiply(BigDecimal.valueOf(c.amount())))
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
            ProductSnapshot snapshot = snapshots.get(item.productId());
            Product productRef = new Product();
            productRef.setId(snapshot.id());
            PurchaseDetail d = new PurchaseDetail();
            d.setPurchase(savedPurchase);
            d.setProduct(productRef);
            d.setMonto(snapshot.price());
            d.setAmount(item.amount());
            return d;
        }).toList();

        purchaseDetailRepository.saveAll(details);
        savedPurchase.setPurchaseDetails(details);

        // Decrementa stock via port (en microservicio: llamada al catalog-service)
        cartItems.forEach(item ->
            productCatalogPort.decrementStock(item.productId(), item.amount())
        );

        // Notifica via port (en microservicio: evento Kafka → notification-service)
        notificationPort.notifyUser(buyerId, "Compra realizada",
                "Tu compra #" + savedPurchase.getId() + " fue procesada con éxito.");

        snapshots.values().forEach(snapshot -> {
            if (snapshot.sellerId() != null && !snapshot.sellerId().equals(buyerId)) {
                notificationPort.notifyUser(snapshot.sellerId(), "Producto vendido",
                        "Tu producto '" + snapshot.name() + "' ha sido vendido en la compra #"
                        + savedPurchase.getId() + ".");
            }
        });

        cartPort.clear(buyerId);
        return mapToDto(savedPurchase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getMyPurchases() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new UnauthorizedException("Usuario no autenticado");
        return purchaseRepository.findByUserId(userId).stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getMySales() {
        Long sellerId = AuthUtil.getAuthenticatedUserId();
        if (sellerId == null) throw new UnauthorizedException("Usuario no autenticado");
        return purchaseRepository.findSalesBySellerId(sellerId).stream().map(this::mapToDto).toList();
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

        if (purchase.getUser() != null) dto.setUserId(purchase.getUser().getId());

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
                }).toList();
            dto.setDetails(detailDtos);
        }
        return dto;
    }
}
