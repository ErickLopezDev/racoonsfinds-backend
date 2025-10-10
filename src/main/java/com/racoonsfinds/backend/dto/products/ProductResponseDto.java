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
    private Long id;
    private String name;
    private Integer stock;
    private String image;
    private BigDecimal price;
    private String description;
    private LocalDate createdDate;
    private Boolean eliminado;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private String userName;
    // TODO INICIALIZAR EL CATEGORY Y USER
    private static final String S3_BASE_URL = "https://racoonsfinds.s3.us-east-1.amazonaws.com/";

    public String getImageUrl() {
        if (image == null || image.isEmpty()) {
            return null;
        }
        return S3_BASE_URL + image;
    }
}