package com.racoonsfinds.backend.dto.wishlist;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponseDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
}
