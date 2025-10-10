package com.racoonsfinds.backend.dto.wishlist;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistRequestDto {
    private Long userId;
    private Long productId;
}
