package com.racoonsfinds.backend.service.int_;

import java.util.List;

import com.racoonsfinds.backend.dto.wishlist.WishlistRequestDto;
import com.racoonsfinds.backend.dto.wishlist.WishlistResponseDto;

public interface WishlistService {
    WishlistResponseDto addToWishlist(WishlistRequestDto dto);
    void removeFromWishlist(Long productId);
    List<WishlistResponseDto> getUserWishlist();
}
