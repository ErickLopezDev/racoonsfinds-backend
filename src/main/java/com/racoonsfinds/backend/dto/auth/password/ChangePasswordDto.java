package com.racoonsfinds.backend.dto.auth.password;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDto {
    @NotBlank
    private String newPassword;
}