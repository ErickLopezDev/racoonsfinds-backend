package com.racoonsfinds.backend.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.dto.auth.verify.VerifyCodeDto;
import com.racoonsfinds.backend.model.RefreshToken;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.security.JwtUtil;
import com.racoonsfinds.backend.service.int_.EmailService;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    // configuración de seguridad
    private final int maxFailedAttempts = 5;
    private final int lockMinutes = 15;
    private final int verificationCodeExpiryMinutes = 15;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto dto) {
        var optUser = userRepository.findByEmail(dto.getEmail());
        if (optUser.isEmpty()) {
            optUser = userRepository.findByUsername(dto.getEmail());
        }
        if (optUser.isEmpty()) {
            throw new RuntimeException("Credenciales inválidas");
        }
        User user = optUser.get();

        // bloqueo por intentos
        if (user.getFailedAttempts() != null && user.getFailedAttempts() >= maxFailedAttempts) {
            if (user.getLastLogin() != null && user.getLastLogin().plusMinutes(lockMinutes).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Cuenta bloqueada temporalmente por varios intentos fallidos. Intenta más tarde.");
            } else {
                // reset lock after time passed
                user.setFailedAttempts(0);
                userRepository.save(user);
            }
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            user.setFailedAttempts((user.getFailedAttempts() == null ? 0 : user.getFailedAttempts()) + 1);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            throw new RuntimeException("Credenciales inválidas");
        }

        // login ok
        user.setFailedAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String access = jwtUtil.generateToken(String.valueOf(user.getId()));
        RefreshToken refresh = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(user.getId(), access, refresh.getToken());
    }

    @Transactional
    public void register(RegisterRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username ya registrado");
        }

        User user = MapperUtil.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setVerified(false);

        // generar código de verificación
        String code = generate6DigitCode();
        user.setVerificationCode(code);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));

        userRepository.save(user);

        // enviar correo
        String subject = "Código de verificación";
        String body = String.format("Tu código de verificación es: %s (válido por %d minutos)", code, verificationCodeExpiryMinutes);
        emailService.sendVerificationEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void verifyCode(VerifyCodeDto dto) {
        var opt = userRepository.findById(dto.getUserId());
        if (opt.isEmpty()) throw new RuntimeException("Usuario no encontrado");
        User user = opt.get();
        if (user.getVerificationCode() == null || user.getCodeExpiry() == null) {
            throw new RuntimeException("No hay código pendiente");
        }
        if (user.getCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado");
        }
        if (!user.getVerificationCode().equals(dto.getCode())) {
            throw new RuntimeException("Código inválido");
        }
        user.setVerified(true);
        user.setVerificationCode(null);
        user.setCodeExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerification(Long userId) {
        var opt = userRepository.findById(userId);
        if (opt.isEmpty()) throw new RuntimeException("Usuario no encontrado");
        User user = opt.get();

        String code = generate6DigitCode();
        user.setVerificationCode(code);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
        userRepository.save(user);

        String subject = "Reenvío código de verificación";
        String body = String.format("Tu nuevo código de verificación es: %s (válido por %d minutos)", code, verificationCodeExpiryMinutes);
        emailService.sendVerificationEmail(user.getEmail(), subject, body);
    }

    private String generate6DigitCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(900_000) + 100_000; // asegura 6 dígitos
        return String.valueOf(number);
    }
}
