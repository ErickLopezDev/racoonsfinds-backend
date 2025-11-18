package com.racoonsfinds.backend.dto.cart;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponseDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private Integer amount;
}
