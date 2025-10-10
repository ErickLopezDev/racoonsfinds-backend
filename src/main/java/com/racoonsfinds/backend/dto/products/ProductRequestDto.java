package com.racoonsfinds.backend.dto.products;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto{
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;

    // Image will be handled separately as MultipartFile in controller

    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Price must have up to 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @Size(max = 750, message = "Description must not exceed 750 characters")
    private String description;

    // private LocalDate createdDate;

    // private Boolean eliminado;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}