package com.racoonsfinds.backend.service.int_;

import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.dto.auth.resend.RequestResendDto;
import com.racoonsfinds.backend.dto.auth.verify.VerifyCodeDto;

public interface AuthService {
  AuthResponseDto login(LoginRequestDto dto);
  AuthResponseDto verifyCode(VerifyCodeDto dto);
  void register(RegisterRequestDto dto);
  void resendVerification(RequestResendDto dto);
  void forgotPassword(String email);
  AuthResponseDto resetPassword(String code, String newPassword);
}
