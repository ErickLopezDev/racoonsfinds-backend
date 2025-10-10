package com.racoonsfinds.backend.service.int_;

import java.util.List;

import com.racoonsfinds.backend.dto.cart.CartRequestDto;
import com.racoonsfinds.backend.dto.cart.CartResponseDto;

public interface CartService {
    CartResponseDto addToCart(CartRequestDto dto);
    void removeFromCart(Long productId);
    List<CartResponseDto> getUserCart();
    void clearCart();
}
