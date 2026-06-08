package com.racoonsfinds.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.dto.cart.CartRequestDto;
import com.racoonsfinds.backend.dto.cart.CartResponseDto;
import com.racoonsfinds.backend.model.Cart;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.CartRepository;
import com.racoonsfinds.backend.service.int_.CartService;
import com.racoonsfinds.backend.service.port.ProductCatalogPort;
import com.racoonsfinds.backend.service.port.ProductSnapshot;
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
            cart.setUser(userRef(userId));
            cart.setProduct(productRef(snapshot.id()));
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

        List<Long> productIds = carts.stream().map(c -> c.getProduct().getId()).toList();
        Map<Long, ProductSnapshot> snapshots = productCatalogPort.findAllByIds(productIds)
                .stream().collect(Collectors.toMap(ProductSnapshot::id, s -> s));

        return carts.stream()
                .map(c -> toDto(c, snapshots.get(c.getProduct().getId())))
                .collect(Collectors.toList());
    }

    @Override
    public void clearCart() {
        cartRepository.deleteAll(cartRepository.findByUserId(AuthUtil.getAuthenticatedUserId()));
    }

    private CartResponseDto toDto(Cart cart, ProductSnapshot snapshot) {
        CartResponseDto dto = new CartResponseDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setProductId(snapshot.id());
        dto.setProductName(snapshot.name());
        dto.setProductImage(s3Service.getFileUrl(snapshot.imageKey()));
        dto.setProductPrice(snapshot.price());
        dto.setAmount(cart.getAmount());
        return dto;
    }

    private static User userRef(Long userId) {
        User ref = new User();
        ref.setId(userId);
        return ref;
    }

    private static Product productRef(Long productId) {
        Product ref = new Product();
        ref.setId(productId);
        return ref;
    }
}
