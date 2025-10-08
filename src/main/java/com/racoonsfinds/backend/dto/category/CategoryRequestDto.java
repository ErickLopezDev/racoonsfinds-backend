package com.racoonsfinds.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDto {
    @NotBlank(message = "El nombre es requerido")
    @Size(max = 45, message = "El nombre no puede tener más de 45 caracteres")
    private String name;
}