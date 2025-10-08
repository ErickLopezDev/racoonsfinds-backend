package com.racoonsfinds.backend.dto.products;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Integer id;
    private String name;
    private Integer stock;
    private String image;
    private BigDecimal price;
    private String description;
    private LocalDate createdDate;
    private Boolean eliminado;
    private Integer categoryId;
    private String categoryName;
    private Integer userId;
    private String userName;
}