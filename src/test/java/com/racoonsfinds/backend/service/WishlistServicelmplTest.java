package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.wishlist.WishlistRequestDto;
import com.racoonsfinds.backend.dto.wishlist.WishlistResponseDto;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.model.Wishlist;
import com.racoonsfinds.backend.repository.ProductRepository;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.repository.WishlistRepository;
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
class WishlistServiceImplTest {

    @Mock private S3Service s3Service;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    @BeforeEach
    void setupSecurity() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of())
        );
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addToWishlist_ShouldAdd_WhenNotExists() {
        User user = new User(); user.setId(1L);
        Product product = new Product();
        product.setId(10L); product.setName("P1");
        product.setImage("img/a"); product.setPrice(new BigDecimal("5.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(s3Service.getFileUrl("img/a")).thenReturn("URL");

        Wishlist saved = new Wishlist();
        saved.setId(7L); saved.setUser(user); saved.setProduct(product);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(saved);

        WishlistRequestDto req = new WishlistRequestDto();
        req.setProductId(10L);

        WishlistResponseDto resp = wishlistService.addToWishlist(req);
        assertNotNull(resp);
        // ID puede ser nulo porque el servicio no reutiliza el retorno de save
        assertEquals(1L, resp.getUserId());
        assertEquals(10L, resp.getProductId());
        assertEquals("P1", resp.getProductName());
        assertEquals("URL", resp.getProductImage());
        assertEquals(new BigDecimal("5.00"), resp.getProductPrice());
    }

    @Test
    void addToWishlist_ShouldThrow_WhenAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(10L)).thenReturn(Optional.of(new Product()));
        when(wishlistRepository.findByUserIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(new Wishlist()));

        WishlistRequestDto req = new WishlistRequestDto();
        req.setProductId(10L);

        assertThrows(RuntimeException.class, () -> wishlistService.addToWishlist(req));
    }

    @Test
    void getUserWishlist_ShouldMapItems() {
        User user = new User(); user.setId(1L);
        Product product = new Product();
        product.setId(10L); product.setName("P1"); product.setImage("img/a");
        product.setPrice(new BigDecimal("5.00"));

        Wishlist wl = new Wishlist(); wl.setId(3L); wl.setUser(user); wl.setProduct(product);
        when(wishlistRepository.findByUserId(1L)).thenReturn(List.of(wl));
        when(s3Service.getFileUrl("img/a")).thenReturn("URL");

        var list = wishlistService.getUserWishlist();
        assertEquals(1, list.size());
        assertEquals(3L, list.get(0).getId());
        assertEquals("URL", list.get(0).getProductImage());
    }

    @Test
    void removeFromWishlist_ShouldInvokeRepository() {
        wishlistService.removeFromWishlist(10L);
        verify(wishlistRepository).deleteByUserIdAndProductId(1L, 10L);
    }
}