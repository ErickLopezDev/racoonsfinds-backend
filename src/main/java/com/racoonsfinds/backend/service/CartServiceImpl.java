package com.racoonsfinds.backend.service;

import java.util.List;
import java.util.stream.Collectors;

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
import com.racoonsfinds.backend.shared.exception.BadRequestException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final S3Service s3Service;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponseDto addToCart(CartRequestDto dto) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        Cart cart = cartRepository.findByUserIdAndProductId(userId, dto.getProductId())
                .orElse(null);

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setAmount(dto.getAmount());
        } else {
            cart.setAmount(cart.getAmount() + dto.getAmount());
        }

        if (cart.getAmount() > product.getStock()) {
            throw new BadRequestException("La cantidad solicitada supera el stock disponible");
        }

        cartRepository.save(cart);
        return toDto(cart);
    }

    @Override
    public void removeFromCart(Long productId) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<CartResponseDto> getUserCart() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        return cartRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void clearCart() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        cartRepository.deleteAll(cartRepository.findByUserId(userId));
    }

    private CartResponseDto toDto(Cart cart) {
        CartResponseDto dto = MapperUtil.map(cart, CartResponseDto.class);
        if (cart.getProduct() != null)
            dto.setProductImage(s3Service.getFileUrl(cart.getProduct().getImage()));
        return dto;
    }
}
