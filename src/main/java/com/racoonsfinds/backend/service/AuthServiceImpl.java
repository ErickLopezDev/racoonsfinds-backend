package com.racoonsfinds.backend.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.dto.auth.resend.RequestResendDto;
import com.racoonsfinds.backend.dto.auth.verify.VerifyCodeDto;
import com.racoonsfinds.backend.model.RefreshToken;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.security.JwtUtil;
import com.racoonsfinds.backend.service.int_.AuthService;
import com.racoonsfinds.backend.service.int_.EmailService;
import com.racoonsfinds.backend.shared.const_.UserStatus;
import com.racoonsfinds.backend.shared.exception.*;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final UserTransactionService userTransactionService;

    // Configuración de seguridad
    public static final int maxFailedAttempts = 5;
    public static final int lockMinutes = 15;
    public static final int verificationCodeExpiryMinutes = 15;

    private Random random = new Random();

    // ==========================================================
    // LOGIN
    // ==========================================================
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponseDto login(LoginRequestDto dto) {

        // Usuario no encontrado
        var optUser = userRepository.findByEmail(dto.getEmail());
        if (optUser.isEmpty()) {
            optUser = userRepository.findByUsername(dto.getEmail());
        }
        if (optUser.isEmpty()) {
            return new AuthResponseDto(null, UserStatus.NOT_FOUND, null, null);
        }

        User user = optUser.get();

        // Usuario bloqueado temporalmente
        if (user.getFailedAttempts() >= maxFailedAttempts) {
            if (user.getLastLogin() != null &&
                user.getLastLogin().plusMinutes(lockMinutes).isAfter(LocalDateTime.now())) {
                return new AuthResponseDto(user.getId(), UserStatus.BLOCKED_TEMP, null, null);
            } else {
                userTransactionService.resetFailedAttempts(user);
            }
        }

        // Contraseña incorrecta
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            userTransactionService.incrementFailedAttempts(user);
            int remaining = Math.max(0, maxFailedAttempts - (user.getFailedAttempts() + 1));
            if (remaining == 0) {
                // Se alcanzó el máximo de intentos → bloqueo temporal
                return new AuthResponseDto(user.getId(), UserStatus.BLOCKED_TEMP, null, null);
            }
            // Credenciales inválidas pero aún puede intentar
            return new AuthResponseDto(user.getId(), UserStatus.NOT_AUTH, null, null);
        }

        // Usuario no verificado (correo pendiente de validación)
        if (!Boolean.TRUE.equals(user.getVerified())) {
            return new AuthResponseDto(user.getId(), UserStatus.NOT_VERIFIED, null, null);
        }

        // Login exitoso
        userTransactionService.resetFailedAttempts(user);
        String access = jwtUtil.generateToken(String.valueOf(user.getId()));
        RefreshToken refresh = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(user.getId(), UserStatus.AUTH_SUCCESS, access, refresh.getToken());
    }

    // ==========================================================
    // VERIFICAR CÓDIGO
    // ==========================================================
    @Transactional
    public AuthResponseDto verifyCode(VerifyCodeDto dto) {

        var opt = userRepository.findByEmail(dto.getEmail());
        if (opt.isEmpty()) {
            // Usuario no existe
            return new AuthResponseDto(null, UserStatus.NOT_FOUND, null, null);
        }

        User user = opt.get();

        // No hay un código pendiente
        if (user.getVerificationCode() == null || user.getCodeExpiry() == null) {
            return new AuthResponseDto(user.getId(), UserStatus.CODE_NOT_REQUESTED, null, null);
        }

        // Código expirado
        if (user.getCodeExpiry().isBefore(LocalDateTime.now())) {
            return new AuthResponseDto(user.getId(), UserStatus.CODE_EXPIRED, null, null);
        }

        // Código inválido
        if (!user.getVerificationCode().equals(dto.getCode())) {
            return new AuthResponseDto(user.getId(), UserStatus.CODE_INVALID, null, null);
        }

        // Usuario ya verificado → no necesita volver a hacerlo
        if (Boolean.TRUE.equals(user.getVerified())) {
            return new AuthResponseDto(user.getId(), UserStatus.AUTH_SUCCESS, null, null);
        }

        // Código correcto → Verificación exitosa
        user.setVerified(true);
        user.setVerificationCode(null);
        user.setCodeExpiry(null);
        userRepository.save(user);

        String access = jwtUtil.generateToken(String.valueOf(user.getId()));
        RefreshToken refresh = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(user.getId(), UserStatus.AUTH_SUCCESS, access, refresh.getToken());
    }

    // ==========================================================
    // REGISTRO Y REENVÍO
    // ==========================================================
    @Transactional
    public void register(RegisterRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("El correo ya está registrado.");
        }
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ConflictException("El nombre de usuario ya está registrado.");
        }

        User user = MapperUtil.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setVerified(false);

        String code = generate6DigitCode();
        user.setVerificationCode(code);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));

        userRepository.save(user);

        String subject = "Código de verificación";
        String body = String.format("Tu código de verificación es: %s (válido por %d minutos)",
                code, verificationCodeExpiryMinutes);
        emailService.sendVerificationEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void resendVerification(RequestResendDto dto) {
        var opt = userRepository.findByEmail(dto.getEmail());
        if (opt.isEmpty()) throw new NotFoundException("Usuario no encontrado.");

        User user = opt.get();
        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new BadRequestException("El usuario ya está verificado.");
        }

        String code = generate6DigitCode();
        user.setVerificationCode(code);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
        userRepository.save(user);

        String subject = "Reenvío de código de verificación";
        String body = String.format("Tu nuevo código de verificación es: %s (válido por %d minutos)",
                code, verificationCodeExpiryMinutes);
        emailService.sendVerificationEmail(user.getEmail(), subject, body);
    }

    // ==========================================================
    // RECUPERAR / RESETEAR CONTRASEÑA
    // ==========================================================
    @Transactional
    public void forgotPassword(String email) {
        var optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) throw new NotFoundException("No existe un usuario con ese correo.");

        User user = optUser.get();

        String code = generate6DigitCode();
        user.setVerificationCode(code);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
        userRepository.save(user);

        String subject = "Recuperación de contraseña";
        String body = String.format("""
            <p>Recibimos una solicitud para restablecer tu contraseña.</p>
            <p>Tu código de verificación es: <b>%s</b> (válido por %d minutos).</p>
            <p>Si no realizaste esta solicitud, puedes ignorar este mensaje.</p>
        """, code, verificationCodeExpiryMinutes);

        emailService.sendPasswordResetEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public AuthResponseDto resetPassword(String code, String newPassword) {

        var userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new UnauthorizedException("Usuario no autenticado.");
        
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado."));

        if (user.getVerificationCode() == null || user.getCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("Código inválido o expirado.");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new UnauthorizedException("El código ingresado no es válido.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setCodeExpiry(null);
        userRepository.save(user);
        
        String access = jwtUtil.generateToken(String.valueOf(user.getId()));
        RefreshToken refresh = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(user.getId(), UserStatus.AUTH_SUCCESS, access, refresh.getToken());
    }

    // AUXILIAR
    private String generate6DigitCode() {
        int number = this.random.nextInt(900_000) + 100_000;
        return String.valueOf(number);
    }
}
