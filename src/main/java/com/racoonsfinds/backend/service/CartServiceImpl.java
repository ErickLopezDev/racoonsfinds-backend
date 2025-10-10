package com.racoonsfinds.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.dto.cart.CartRequestDto;
import com.racoonsfinds.backend.dto.cart.CartResponseDto;
import com.racoonsfinds.backend.model.Cart;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.CartRepository;
import com.racoonsfinds.backend.repository.ProductRepository;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.service.int_.CartService;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponseDto addToCart(CartRequestDto dto) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepository.findByUserIdAndProductId(userId, dto.getProductId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setProduct(product);
                    newCart.setAmount(dto.getAmount());
                    return newCart;
                });

        cart.setAmount(dto.getAmount());
        cartRepository.save(cart);

        return MapperUtil.map(cart, CartResponseDto.class);
    }

    @Override
    public void removeFromCart(Long productId) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<CartResponseDto> getUserCart() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        List<Cart> cartList = cartRepository.findByUserId(userId);
        return MapperUtil.mapList(cartList, CartResponseDto.class);
    }

    @Override
    public void clearCart() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        List<Cart> items = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(items);
    }
}
