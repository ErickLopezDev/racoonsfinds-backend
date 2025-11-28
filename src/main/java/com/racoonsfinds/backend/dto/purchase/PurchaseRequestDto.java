package com.racoonsfinds.backend.dto.purchase;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDto {

    @NotNull(message = "La dirección es obligatoria")
    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 500, message = "La dirección no puede superar los 500 caracteres")
    private String direccion;

    @NotNull(message = "El distrito es obligatorio")
    @NotBlank(message = "El distrito no puede estar vacío")
    @Size(max = 500, message = "El distrito no puede superar los 500 caracteres")
    private String distrito;
    
    @NotNull(message = "La provincia es obligatoria")
    @NotBlank(message = "La provincia no puede estar vacía")
    @Size(max = 500, message = "La provincia no puede superar los 500 caracteres")
    private String provincia;

    @NotNull(message = "La referencia es obligatoria")
    @NotBlank(message = "La referencia no puede estar vacía")
    @Size(max = 500, message = "La referencia no puede superar los 500 caracteres")
    private String referencia;

}
