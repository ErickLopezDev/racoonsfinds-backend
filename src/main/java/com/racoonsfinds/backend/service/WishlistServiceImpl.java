package com.racoonsfinds.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.dto.wishlist.WishlistRequestDto;
import com.racoonsfinds.backend.dto.wishlist.WishlistResponseDto;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.model.Wishlist;
import com.racoonsfinds.backend.repository.ProductRepository;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.repository.WishlistRepository;
import com.racoonsfinds.backend.service.int_.WishlistService;
import com.racoonsfinds.backend.shared.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final S3Service s3Service;
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public WishlistResponseDto addToWishlist(WishlistRequestDto dto) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        wishlistRepository.findByUserIdAndProductId(userId, dto.getProductId())
                .ifPresent(wl -> {
                    throw new RuntimeException("Product already in wishlist");
                });

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);

        return buildResponseDto(wishlist);
    }

    @Override
    public void removeFromWishlist(Long productId) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<WishlistResponseDto> getUserWishlist() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        List<Wishlist> wishlist = wishlistRepository.findByUserId(userId);
        return wishlist.stream()
                .map(this::buildResponseDto)
                .collect(Collectors.toList());
    }

    private WishlistResponseDto buildResponseDto(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        User user = wishlist.getUser();

        WishlistResponseDto dto = new WishlistResponseDto();
        dto.setId(wishlist.getId());
        dto.setUserId(user != null ? user.getId() : null);
        dto.setProductId(product != null ? product.getId() : null);
        dto.setProductName(product != null ? product.getName() : null);
        dto.setProductImage(product != null ? s3Service.getFileUrl(product.getImage()) : null);
        dto.setProductPrice(product != null ? product.getPrice() : null);
        return dto;
    }
}
