package com.racoonsfinds.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.dto.auth.verify.VerifyCodeDto;
import com.racoonsfinds.backend.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto dto) {
        authService.register(dto);
        return ResponseEntity.ok().body("Usuario registrado. Revisa tu correo para confirmar.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        AuthResponseDto resp = authService.login(dto);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyCodeDto dto) {
        authService.verifyCode(dto);
        return ResponseEntity.ok("Verificado correctamente");
    }

    @PostMapping("/resend/{userId}")
    public ResponseEntity<?> resend(@PathVariable Long userId) {
        authService.resendVerification(userId);
        return ResponseEntity.ok("Código reenviado");
    }
}