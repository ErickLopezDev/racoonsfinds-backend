package com.racoonsfinds.backend.identity.service;

import com.racoonsfinds.backend.identity.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.identity.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.identity.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.identity.dto.auth.resend.RequestResendDto;
import com.racoonsfinds.backend.identity.dto.auth.verify.VerifyCodeDto;

public interface AuthService {
  AuthResponseDto login(LoginRequestDto dto);
  AuthResponseDto verifyCode(VerifyCodeDto dto);
  void register(RegisterRequestDto dto);
  void resendVerification(RequestResendDto dto);
  void forgotPassword(String email);
  AuthResponseDto resetPassword(String code, String newPassword);
}
