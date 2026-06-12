package com.racoonsfinds.backend.catalog.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private Long id;
    private String name;
    private Integer stock;
    private BigDecimal price;
    private String description;
    private Boolean eliminado;
    private Long categoryId;
}
