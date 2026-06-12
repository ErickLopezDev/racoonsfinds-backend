package com.racoonsfinds.backend.cart.service;

import com.racoonsfinds.backend.platform.storage.S3Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.cart.dto.CartRequestDto;
import com.racoonsfinds.backend.cart.dto.CartResponseDto;
import com.racoonsfinds.backend.cart.domain.Cart;
import com.racoonsfinds.backend.cart.repository.CartRepository;
import com.racoonsfinds.backend.cart.service.CartService;
import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.catalog.port.ProductSnapshot;
import com.racoonsfinds.backend.shared.exception.BadRequestException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final S3Service s3Service;
    private final CartRepository cartRepository;
    private final ProductCatalogPort productCatalogPort;

    @Override
    public CartResponseDto addToCart(CartRequestDto dto) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        ProductSnapshot snapshot = productCatalogPort.findById(dto.getProductId());

        Cart cart = cartRepository.findByUserIdAndProductId(userId, dto.getProductId())
                .orElse(null);

        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(snapshot.id());
            cart.setAmount(dto.getAmount());
        } else {
            cart.setAmount(cart.getAmount() + dto.getAmount());
        }

        if (cart.getAmount() > snapshot.stock()) {
            throw new BadRequestException("La cantidad solicitada supera el stock disponible");
        }

        cartRepository.save(cart);
        return toDto(cart, snapshot);
    }

    @Override
    public void removeFromCart(Long productId) {
        cartRepository.deleteByUserIdAndProductId(AuthUtil.getAuthenticatedUserId(), productId);
    }

    @Override
    public List<CartResponseDto> getUserCart() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        List<Cart> carts = cartRepository.findByUserId(userId);

        List<Long> productIds = carts.stream().map(Cart::getProductId).toList();
        Map<Long, ProductSnapshot> snapshots = productCatalogPort.findAllByIds(productIds)
                .stream().collect(Collectors.toMap(ProductSnapshot::id, s -> s));

        return carts.stream()
                .map(c -> toDto(c, snapshots.get(c.getProductId())))
                .collect(Collectors.toList());
    }

    @Override
    public void clearCart() {
        cartRepository.deleteAll(cartRepository.findByUserId(AuthUtil.getAuthenticatedUserId()));
    }

    private CartResponseDto toDto(Cart cart, ProductSnapshot snapshot) {
        CartResponseDto dto = new CartResponseDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setProductId(snapshot.id());
        dto.setProductName(snapshot.name());
        dto.setProductImage(s3Service.getFileUrl(snapshot.imageKey()));
        dto.setProductPrice(snapshot.price());
        dto.setAmount(cart.getAmount());
        return dto;
    }
}
