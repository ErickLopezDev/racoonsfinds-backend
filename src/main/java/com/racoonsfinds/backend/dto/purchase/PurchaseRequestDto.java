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
    private String address;

    @NotNull(message = "El distrito es obligatorio")
    @NotBlank(message = "El distrito no puede estar vacío")
    @Size(max = 500, message = "El distrito no puede superar los 500 caracteres")
    private String district;

    @NotNull(message = "La region es obligatoria")
    @NotBlank(message = "La region no puede estar vacío")
    @Size(max = 500, message = "La region no puede superar los 500 caracteres")
    private String region ;

    @NotNull(message = "El nombre de contacto es obligatorio")
    @NotBlank(message = "El nombre de contacto puede estar vacío")
    @Size(max = 300, message = "El nombre de contacto no puede superar los 300 caracteres")
    private String contactName;
    
    @NotNull(message = "El celular de contacto es obligatorio")
    @NotBlank(message = "El celular de contacto no puede estar vacío")
    @Size(max = 15, message = "El celular de contacto no puede superar los 15 caracteres")
    private String phoneNumber;
    
    @NotNull(message = "La provincia es obligatoria")
    @NotBlank(message = "La provincia no puede estar vacía")
    @Size(max = 500, message = "La provincia no puede superar los 500 caracteres")
    private String province;

    @NotNull(message = "La referencia es obligatoria")
    @NotBlank(message = "La referencia no puede estar vacía")
    @Size(max = 500, message = "La referencia no puede superar los 500 caracteres")
    private String reference;

}
