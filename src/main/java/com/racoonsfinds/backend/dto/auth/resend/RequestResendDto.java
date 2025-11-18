package com.racoonsfinds.backend.dto.auth.resend;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RequestResendDto {
  @Email
  private String email;
}
