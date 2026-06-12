package com.racoonsfinds.backend.cart.service;

import java.util.List;

import com.racoonsfinds.backend.cart.dto.CartRequestDto;
import com.racoonsfinds.backend.cart.dto.CartResponseDto;

public interface CartService {
    CartResponseDto addToCart(CartRequestDto dto);
    void removeFromCart(Long productId);
    List<CartResponseDto> getUserCart();
    void clearCart();
    // void UpdateQuantity(Long productId, int quantity);
}
