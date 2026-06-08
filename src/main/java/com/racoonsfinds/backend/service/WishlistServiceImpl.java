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
import com.racoonsfinds.backend.shared.exception.ConflictException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

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
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        wishlistRepository.findByUserIdAndProductId(userId, dto.getProductId())
                .ifPresent(wl -> {
                    throw new ConflictException("El producto ya está en tu wishlist");
                });

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);

        return toDto(wishlist);
    }

    @Override
    public void removeFromWishlist(Long productId) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<WishlistResponseDto> getUserWishlist() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        return wishlistRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private WishlistResponseDto toDto(Wishlist wishlist) {
        WishlistResponseDto dto = MapperUtil.map(wishlist, WishlistResponseDto.class);
        if (wishlist.getProduct() != null)
            dto.setProductImage(s3Service.getFileUrl(wishlist.getProduct().getImage()));
        return dto;
    }
}
