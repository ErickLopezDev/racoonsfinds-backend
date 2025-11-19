package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.model.*;
import com.racoonsfinds.backend.repository.CartRepository;
import com.racoonsfinds.backend.repository.PurchaseDetailRepository;
import com.racoonsfinds.backend.repository.PurchaseRepository;
import com.racoonsfinds.backend.service.int_.NotificationService;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private PurchaseRepository purchaseRepository;
    @Mock private PurchaseDetailRepository purchaseDetailRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @BeforeEach
    void setupAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @AfterEach
    void clearAuth() { SecurityContextHolder.clearContext(); }

    private Cart buildCartItem(long userId, long productId, int amount, String name, String price) {
        User user = new User(); user.setId(userId);
        Product product = new Product();
        product.setId(productId);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        Cart c = new Cart();
        c.setUser(user); c.setProduct(product); c.setAmount(amount);
        return c;
    }

    // @Test
    // void purchaseFromCart_ShouldCreatePurchaseAndClearCart() {
    //     Cart item1 = buildCartItem(1L, 10L, 2, "A", "5.00");
    //     Cart item2 = buildCartItem(1L, 11L, 1, "B", "7.50");
    //     when(cartRepository.findByUserId(1L)).thenReturn(List.of(item1, item2));

    //     Purchase saved = new Purchase(); saved.setId(99L); saved.setUser(item1.getUser());
    //     when(purchaseRepository.save(any(Purchase.class))).thenReturn(saved);

    //     ResponseEntity<ApiResponse<PurchaseResponseDto>> response = purchaseService.purchaseFromCart("desc");

    //     assertNotNull(response);
    //     assertEquals(201, response.getStatusCode().value());
    //     assertNotNull(response.getBody());
    //     assertNotNull(response.getBody().getData());
    //     assertEquals(99L, response.getBody().getData().getId());

    //     verify(purchaseDetailRepository).saveAll(anyList());
    //     verify(notificationService).createNotification(eq(1L), anyString(), anyString());
    //     verify(cartRepository).deleteByUserId(1L);
    // }

    @Test
    void purchaseFromCart_ShouldThrow_WhenCartEmpty() {
        when(cartRepository.findByUserId(1L)).thenReturn(List.of());
        assertThrows(NotFoundException.class, () -> purchaseService.purchaseFromCart(null));
    }

    // @Test
    // void purchaseOne_ShouldCreateSinglePurchase() {
    //     Cart item = buildCartItem(1L, 10L, 3, "A", "2.00");
    //     when(cartRepository.findById(5L)).thenReturn(Optional.of(item));

    //     Purchase saved = new Purchase(); saved.setId(50L); saved.setUser(item.getUser());
    //     when(purchaseRepository.save(any(Purchase.class))).thenReturn(saved);

    //     var resp = purchaseService.purchaseOne(5L, null);
    //     assertEquals(201, resp.getStatusCode().value());
    //     assertEquals(50L, resp.getBody().getData().getId());

    //     verify(purchaseDetailRepository).save(any(PurchaseDetail.class));
    //     verify(notificationService).createNotification(eq(1L), anyString(), anyString());
    //     verify(cartRepository).delete(item);
    // }

    @Test
    void purchaseOne_ShouldThrow_WhenNotOwner() {
        Cart item = buildCartItem(2L, 10L, 1, "A", "1.00");
        when(cartRepository.findById(5L)).thenReturn(Optional.of(item));
        assertThrows(NotFoundException.class, () -> purchaseService.purchaseOne(5L, "d"));
    }

    @Test
    void purchaseOne_ShouldThrow_WhenNotFound() {
        when(cartRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> purchaseService.purchaseOne(5L, "d"));
    }
}
