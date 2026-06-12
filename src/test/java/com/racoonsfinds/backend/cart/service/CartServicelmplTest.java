package com.racoonsfinds.backend.cart.service;

import com.racoonsfinds.backend.cart.dto.CartRequestDto;
import com.racoonsfinds.backend.cart.dto.CartResponseDto;
import com.racoonsfinds.backend.cart.domain.Cart;
import com.racoonsfinds.backend.cart.repository.CartRepository;
import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.catalog.port.ProductSnapshot;
import com.racoonsfinds.backend.platform.storage.S3Service;
import com.racoonsfinds.backend.shared.exception.BadRequestException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private S3Service s3Service;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductCatalogPort productCatalogPort;

    @InjectMocks
    private CartServiceImpl cartService;

    private ProductSnapshot snapshot(int stock) {
        return new ProductSnapshot(10L, "Prod", new BigDecimal("19.99"), "img/key.png", stock, 99L);
    }

    @BeforeEach
    void setupSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addToCart_ShouldCreateNewCart_WhenNotExisting() {
        when(productCatalogPort.findById(10L)).thenReturn(snapshot(100));
        when(cartRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(s3Service.getFileUrl("img/key.png")).thenReturn("https://bucket/region/img/key.png");

        CartRequestDto dto = new CartRequestDto();
        dto.setProductId(10L);
        dto.setAmount(2);

        CartResponseDto resp = cartService.addToCart(dto);

        assertNotNull(resp);
        assertEquals(1L, resp.getUserId());
        assertEquals(10L, resp.getProductId());
        assertEquals("Prod", resp.getProductName());
        assertEquals("https://bucket/region/img/key.png", resp.getProductImage());
        assertEquals(new BigDecimal("19.99"), resp.getProductPrice());
        assertEquals(2, resp.getAmount());

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addToCart_ShouldIncrementExistingCart_WhenAlreadyExists() {
        Cart existing = new Cart();
        existing.setId(50L);
        existing.setUserId(1L);
        existing.setProductId(10L);
        existing.setAmount(1);

        when(productCatalogPort.findById(10L)).thenReturn(snapshot(100));
        when(cartRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
        when(s3Service.getFileUrl("img/key.png")).thenReturn("URL");

        CartRequestDto dto = new CartRequestDto();
        dto.setProductId(10L);
        dto.setAmount(5);

        CartResponseDto resp = cartService.addToCart(dto);

        // El servicio suma la cantidad nueva a la existente: 1 + 5 = 6
        assertEquals(6, resp.getAmount());
        verify(cartRepository).save(existing);
    }

    @Test
    void addToCart_ShouldThrow_WhenAmountExceedsStock() {
        when(productCatalogPort.findById(10L)).thenReturn(snapshot(3));
        when(cartRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        CartRequestDto dto = new CartRequestDto();
        dto.setProductId(10L);
        dto.setAmount(5);

        assertThrows(BadRequestException.class, () -> cartService.addToCart(dto));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getUserCart_ShouldMapItems() {
        Cart item = new Cart();
        item.setId(5L);
        item.setUserId(1L);
        item.setProductId(10L);
        item.setAmount(3);

        when(cartRepository.findByUserId(1L)).thenReturn(List.of(item));
        when(productCatalogPort.findAllByIds(List.of(10L))).thenReturn(List.of(snapshot(100)));
        when(s3Service.getFileUrl("img/key.png")).thenReturn("URL");

        List<CartResponseDto> list = cartService.getUserCart();
        assertEquals(1, list.size());
        CartResponseDto dto = list.get(0);
        assertEquals(5L, dto.getId());
        assertEquals(3, dto.getAmount());
        assertEquals("URL", dto.getProductImage());
    }

    @Test
    void removeFromCart_ShouldInvokeRepositoryDelete() {
        cartService.removeFromCart(99L);
        verify(cartRepository, times(1)).deleteByUserIdAndProductId(1L, 99L);
    }

    @Test
    void clearCart_ShouldDeleteAllItemsForUser() {
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(new Cart(), new Cart()));
        cartService.clearCart();
        verify(cartRepository).deleteAll(anyList());
    }
}
