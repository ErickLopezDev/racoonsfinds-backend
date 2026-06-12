package com.racoonsfinds.backend.identity.dto.auth.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ForgotPasswordDto {
    @NotBlank
    @Email
    private String email;
}
