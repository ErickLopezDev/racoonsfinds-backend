package com.racoonsfinds.backend.cart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDto {
    private Long productId;
    private Integer amount;
}
