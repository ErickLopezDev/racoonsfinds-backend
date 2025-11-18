package com.racoonsfinds.backend.dto.purchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponseDto {

    private Long id;
    private LocalDate date;
    private BigDecimal monto;
    private String description;
    private Long userId;
    private List<PurchaseDetailResponseDto> details;
}