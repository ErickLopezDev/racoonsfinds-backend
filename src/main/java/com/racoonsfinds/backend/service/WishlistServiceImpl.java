package com.racoonsfinds.backend.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.dto.wishlist.WishlistRequestDto;
import com.racoonsfinds.backend.dto.wishlist.WishlistResponseDto;
import com.racoonsfinds.backend.model.Product;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.model.Wishlist;
import com.racoonsfinds.backend.repository.WishlistRepository;
import com.racoonsfinds.backend.service.int_.WishlistService;
import com.racoonsfinds.backend.service.port.ProductCatalogPort;
import com.racoonsfinds.backend.service.port.ProductSnapshot;
import com.racoonsfinds.backend.shared.exception.ConflictException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final S3Service s3Service;
    private final WishlistRepository wishlistRepository;
    private final ProductCatalogPort productCatalogPort;

    @Override
    public WishlistResponseDto addToWishlist(WishlistRequestDto dto) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        ProductSnapshot snapshot = productCatalogPort.findById(dto.getProductId());

        wishlistRepository.findByUserIdAndProductId(userId, dto.getProductId())
                .ifPresent(wl -> { throw new ConflictException("El producto ya está en tu wishlist"); });

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(userRef(userId));
        wishlist.setProduct(productRef(snapshot.id()));
        wishlistRepository.save(wishlist);

        return toDto(wishlist, snapshot, userId);
    }

    @Override
    public void removeFromWishlist(Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(AuthUtil.getAuthenticatedUserId(), productId);
    }

    @Override
    public List<WishlistResponseDto> getUserWishlist() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);

        List<Long> productIds = wishlists.stream().map(w -> w.getProduct().getId()).toList();
        Map<Long, ProductSnapshot> snapshots = productCatalogPort.findAllByIds(productIds)
                .stream().collect(Collectors.toMap(ProductSnapshot::id, s -> s));

        return wishlists.stream()
                .map(w -> toDto(w, snapshots.get(w.getProduct().getId()), userId))
                .collect(Collectors.toList());
    }

    private WishlistResponseDto toDto(Wishlist wishlist, ProductSnapshot snapshot, Long userId) {
        WishlistResponseDto dto = new WishlistResponseDto();
        dto.setId(wishlist.getId());
        dto.setUserId(userId);
        dto.setProductId(snapshot.id());
        dto.setProductName(snapshot.name());
        dto.setProductImage(s3Service.getFileUrl(snapshot.imageKey()));
        dto.setProductPrice(snapshot.price());
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
