package com.racoonsfinds.backend.dto.auth.verify;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VerifyCodeDto {
    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 6, max = 6)
    private String code;
}
