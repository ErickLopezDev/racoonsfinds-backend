package com.racoonsfinds.backend.wishlist.service;

import com.racoonsfinds.backend.platform.storage.S3Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.wishlist.dto.WishlistRequestDto;
import com.racoonsfinds.backend.wishlist.dto.WishlistResponseDto;
import com.racoonsfinds.backend.wishlist.domain.Wishlist;
import com.racoonsfinds.backend.wishlist.repository.WishlistRepository;
import com.racoonsfinds.backend.wishlist.service.WishlistService;
import com.racoonsfinds.backend.catalog.port.ProductCatalogPort;
import com.racoonsfinds.backend.catalog.port.ProductSnapshot;
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
        wishlist.setUserId(userId);
        wishlist.setProductId(snapshot.id());
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

        List<Long> productIds = wishlists.stream().map(Wishlist::getProductId).toList();
        Map<Long, ProductSnapshot> snapshots = productCatalogPort.findAllByIds(productIds)
                .stream().collect(Collectors.toMap(ProductSnapshot::id, s -> s));

        return wishlists.stream()
                .map(w -> toDto(w, snapshots.get(w.getProductId()), userId))
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
}
