package com.racoonsfinds.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.password.ChangePasswordDto;
import com.racoonsfinds.backend.dto.auth.password.ForgotPasswordDto;
import com.racoonsfinds.backend.dto.auth.password.ResetPasswordDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.dto.auth.resend.RequestResendDto;
import com.racoonsfinds.backend.dto.auth.verify.VerifyCodeDto;
import com.racoonsfinds.backend.service.int_.AuthService;
import com.racoonsfinds.backend.shared.const_.UserStatus;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequestDto dto) {
        authService.register(dto);
        return ResponseUtil.created("Usuario registrado. Revisa tu correo para confirmar.");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto dto) {
        AuthResponseDto response = authService.login(dto);
        
        if (response.getStatus() == UserStatus.AUTH_SUCCESS) {
            return ResponseUtil.ok("Login exitoso", response);
        } else {
            return ResponseUtil.unauthorized("Credenciales inválidas", response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verify(@Valid @RequestBody VerifyCodeDto dto) {
        AuthResponseDto verified = authService.verifyCode(dto);
        return ResponseUtil.ok("Cuenta verificada correctamente", verified);
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resend(@RequestBody RequestResendDto dto) {
        authService.resendVerification(dto);
        return ResponseUtil.ok("Código de verificación reenviado correctamente");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordDto dto) {
        authService.forgotPassword(dto.getEmail());
        return ResponseUtil.ok("Se ha enviado un enlace de recuperación a tu correo electrónico.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthResponseDto>> resetPassword(@RequestBody ResetPasswordDto dto) {
        AuthResponseDto passwordUpdatedAuth = authService.resetPassword(dto.getCode(), dto.getNewPassword());
        return ResponseUtil.ok("Cuenta verificada correctamente", passwordUpdatedAuth);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<AuthResponseDto>> changePassword(@RequestParam("token") String token, @Valid @RequestBody ChangePasswordDto dto) {
        AuthResponseDto passwordUpdatedAuth = authService.changePassword(token, dto.getNewPassword());
        return ResponseUtil.ok("Contraseña cambiada correctamente", passwordUpdatedAuth);
    }
}