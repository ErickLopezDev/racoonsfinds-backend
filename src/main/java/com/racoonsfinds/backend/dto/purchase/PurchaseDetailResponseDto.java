package com.racoonsfinds.backend.dto.purchase;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetailResponseDto {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal monto;
    private Integer amount;
}
