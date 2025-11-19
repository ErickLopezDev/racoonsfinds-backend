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
import com.racoonsfinds.backend.shared.utils.AuthUtil;

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

        return buildResponseDto(cart);
    }

    @Override
    public void removeFromCart(Long productId) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<CartResponseDto> getUserCart() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        List<Cart> carts = cartRepository.findByUserId(userId);
         return carts.stream()
                .map(this::buildResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void clearCart() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        List<Cart> items = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(items);
    }

    // @Transactional
    // public CartItemresponseDto updateQuantity(Long productId, UpdateCartQuantityDto request) {
    //     Long userId = AuthUtil.getAuthenticatedUserId();

    //     Product product = productRepository.findById(productId)
    //             .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

    //     Cart cartItem = cartRepository.findByUserIdAndProductId(userId, productId)
    //             .orElseThrow(() -> new ResourceNotFoundException("Producto no está en el carrito"));

    //     int newQuantity = request.getQuantity();

    //     if (newQuantity <= 0) {
    //         cartRepository.delete(cartItem);
    //         return null; // o devolver una respuesta vacía con un código 204 desde el controller
    //     }

    //     // validar stock disponible
    //     if (newQuantity > product.getStock()) {
    //         throw new IllegalArgumentException("Cantidad supera el stock disponible");
    //     }

    //     cartItem.setQuantity(newQuantity);
    //     cartRepository.save(cartItem);

    //     return new CartItemResponseDto(
    //         product.getId(),
    //         product.getName(),
    //         cartItem.getQuantity(),
    //         product.getPrice() * cartItem.getQuantity()
    //     );
    // }

    private CartResponseDto buildResponseDto(Cart cart) {
        Product product = cart.getProduct();
        User user = cart.getUser();

        CartResponseDto dto = new CartResponseDto();
        dto.setId(cart.getId());
        dto.setUserId(user != null ? user.getId() : null);
        dto.setProductId(product != null ? product.getId() : null);
        dto.setProductName(product != null ? product.getName() : null);
        dto.setProductImage(product != null ? s3Service.getFileUrl(product.getImage()) : null);
        dto.setProductPrice(product != null ? product.getPrice() : null);
        dto.setAmount(cart.getAmount());
        return dto;
    }
}
