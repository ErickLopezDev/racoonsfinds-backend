package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.cart.CartRequestDto;
import com.racoonsfinds.backend.dto.cart.CartResponseDto;
import com.racoonsfinds.backend.model.Cart;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.CartRepository;
import com.racoonsfinds.backend.repository.ProductRepository;
import com.racoonsfinds.backend.repository.UserRepository;
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
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

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
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Prod");
        product.setImage("img/key.png");
        product.setPrice(new BigDecimal("19.99"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(s3Service.getFileUrl("img/key.png")).thenReturn("https://bucket/region/img/key.png");

        Cart saved = new Cart();
        saved.setId(100L);
        saved.setUser(user);
        saved.setProduct(product);
        saved.setAmount(2);
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        CartRequestDto dto = new CartRequestDto();
        dto.setProductId(10L);
        dto.setAmount(2);

        CartResponseDto resp = cartService.addToCart(dto);

        assertNotNull(resp);
        // ID puede ser nulo porque el servicio no reutiliza el retorno de save
        assertEquals(1L, resp.getUserId());
        assertEquals(10L, resp.getProductId());
        assertEquals("Prod", resp.getProductName());
        assertEquals("https://bucket/region/img/key.png", resp.getProductImage());
        assertEquals(new BigDecimal("19.99"), resp.getProductPrice());
        assertEquals(2, resp.getAmount());

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addToCart_ShouldUpdateExistingCart_WhenAlreadyExists() {
        User user = new User(); user.setId(1L);
        Product product = new Product(); product.setId(10L);

        Cart existing = new Cart();
        existing.setId(50L);
        existing.setUser(user);
        existing.setProduct(product);
        existing.setAmount(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(existing)).thenReturn(existing);

        CartRequestDto dto = new CartRequestDto();
        dto.setProductId(10L);
        dto.setAmount(5);

        CartResponseDto resp = cartService.addToCart(dto);

        assertEquals(5, resp.getAmount());
        verify(cartRepository).save(existing);
    }

    @Test
    void getUserCart_ShouldMapItems() {
        User user = new User(); user.setId(1L);
        Product product = new Product();
        product.setId(10L);
        product.setName("Prod");
        product.setImage("img/key.png");
        product.setPrice(new BigDecimal("9.50"));

        Cart item = new Cart();
        item.setId(5L);
        item.setUser(user);
        item.setProduct(product);
        item.setAmount(3);

        when(cartRepository.findByUserId(1L)).thenReturn(List.of(item));
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
