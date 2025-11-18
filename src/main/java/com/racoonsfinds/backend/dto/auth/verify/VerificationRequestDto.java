package com.racoonsfinds.backend.dto.auth.verify;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationRequestDto(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    String email,

    @NotBlank(message = "El código de verificación es obligatorio")
    String code
) {}
