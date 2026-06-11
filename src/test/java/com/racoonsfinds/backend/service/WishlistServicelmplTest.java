package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.wishlist.WishlistRequestDto;
import com.racoonsfinds.backend.dto.wishlist.WishlistResponseDto;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.model.Wishlist;
import com.racoonsfinds.backend.repository.WishlistRepository;
import com.racoonsfinds.backend.service.port.ProductCatalogPort;
import com.racoonsfinds.backend.service.port.ProductSnapshot;
import com.racoonsfinds.backend.shared.exception.ConflictException;
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
    @Mock private ProductCatalogPort productCatalogPort;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private ProductSnapshot snapshot() {
        return new ProductSnapshot(10L, "P1", new BigDecimal("5.00"), "img/a", 100, 99L);
    }

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
        when(productCatalogPort.findById(10L)).thenReturn(snapshot());
        when(wishlistRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(s3Service.getFileUrl("img/a")).thenReturn("URL");

        WishlistRequestDto req = new WishlistRequestDto();
        req.setProductId(10L);

        WishlistResponseDto resp = wishlistService.addToWishlist(req);
        assertNotNull(resp);
        assertEquals(1L, resp.getUserId());
        assertEquals(10L, resp.getProductId());
        assertEquals("P1", resp.getProductName());
        assertEquals("URL", resp.getProductImage());
        assertEquals(new BigDecimal("5.00"), resp.getProductPrice());
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_ShouldThrow_WhenAlreadyExists() {
        when(productCatalogPort.findById(10L)).thenReturn(snapshot());
        when(wishlistRepository.findByUserIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(new Wishlist()));

        WishlistRequestDto req = new WishlistRequestDto();
        req.setProductId(10L);

        assertThrows(ConflictException.class, () -> wishlistService.addToWishlist(req));
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void getUserWishlist_ShouldMapItems() {
        User user = new User(); user.setId(1L);
        Product product = new Product(); product.setId(10L);

        Wishlist wl = new Wishlist(); wl.setId(3L); wl.setUser(user); wl.setProduct(product);
        when(wishlistRepository.findByUserId(1L)).thenReturn(List.of(wl));
        when(productCatalogPort.findAllByIds(List.of(10L))).thenReturn(List.of(snapshot()));
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
