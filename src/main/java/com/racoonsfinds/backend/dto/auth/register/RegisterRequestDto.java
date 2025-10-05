package com.racoonsfinds.backend.dto.auth.register;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto{
    @NotBlank
    @Size(max = 45)
    private String username;

    @NotBlank
    @Email
    @Size(max = 45)
    private String email;

    @NotBlank
    @Size(min = 8, max = 70)
    private String password;

    @NotNull
    private LocalDate birthDate;
}