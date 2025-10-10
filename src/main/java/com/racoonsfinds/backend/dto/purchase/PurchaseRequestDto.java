package com.racoonsfinds.backend.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDto {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que 0")
    private BigDecimal monto;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;

    @NotEmpty(message = "Debe haber al menos un detalle de compra")
    private List<PurchaseDetailRequestDto> details;
}
