package com.racoonsfinds.backend.dto.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDto {
    private Long userId;
    private Long productId;
    private Integer amount;
}
