package com.racoonsfinds.backend.wishlist.service;

import java.util.List;

import com.racoonsfinds.backend.wishlist.dto.WishlistRequestDto;
import com.racoonsfinds.backend.wishlist.dto.WishlistResponseDto;

public interface WishlistService {
    WishlistResponseDto addToWishlist(WishlistRequestDto dto);
    void removeFromWishlist(Long productId);
    List<WishlistResponseDto> getUserWishlist();
}
