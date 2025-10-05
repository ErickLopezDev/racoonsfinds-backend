package com.racoonsfinds.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDto {
    @NotNull
    private Long userId;

    @NotBlank
    private String code;

    @NotBlank
    private String newPassword;
}
