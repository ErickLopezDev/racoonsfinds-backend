package com.racoonsfinds.backend.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
import com.racoonsfinds.backend.service.int_.EmailService;
import com.racoonsfinds.backend.shared.exception.ApiException;
import com.racoonsfinds.backend.shared.exception.BadRequestException;
import com.racoonsfinds.backend.shared.exception.ConflictException;
import com.racoonsfinds.backend.shared.exception.ForbiddenException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.exception.UnauthorizedException;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
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

    // ------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponseDto login(LoginRequestDto dto) {
        var optUser = userRepository.findByEmail(dto.getEmail());
        if (optUser.isEmpty()) optUser = userRepository.findByUsername(dto.getEmail());
        if (optUser.isEmpty()) throw new BadRequestException("Credenciales inválidas");

        User user = optUser.get();

        // bloqueo temporal
        if (user.getFailedAttempts() >= maxFailedAttempts) {
            if (user.getLastLogin() != null && user.getLastLogin().plusMinutes(lockMinutes).isAfter(LocalDateTime.now())) {
                throw new ForbiddenException("Cuenta bloqueada temporalmente. Intenta nuevamente en " + lockMinutes + " minutos.");
            } else {
                resetFailedAttempts(user);
            }
        }

        // contraseña incorrecta
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            incrementFailedAttempts(user);
            int remaining = Math.max(0, maxFailedAttempts - (user.getFailedAttempts() + 1));
            String msg = (remaining > 0)
                    ? "Contraseña incorrecta. Te quedan " + remaining + " intento(s) antes del bloqueo."
                    : "Cuenta bloqueada temporalmente por varios intentos fallidos.";
            throw new UnauthorizedException(msg);
        }

        // usuario no verificado
        if (!Boolean.TRUE.equals(user.getVerified())) {
            throw new ForbiddenException("Usuario no verificado. Revisa tu correo electrónico.");
        }

        // login exitoso
        resetFailedAttempts(user);
        String access = jwtUtil.generateToken(String.valueOf(user.getId()));
        RefreshToken refresh = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(user.getId(), access, refresh.getToken());
    }

    // ------------------------------------------------------------
    // REGISTRO
    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
    // VERIFICAR CÓDIGO
    // ------------------------------------------------------------
    @Transactional
    public void verifyCode(VerifyCodeDto dto) {
        var opt = userRepository.findByEmail(dto.getEmail());
        if (opt.isEmpty()) throw new NotFoundException("Usuario no encontrado.");

        User user = opt.get();
        if (user.getVerificationCode() == null || user.getCodeExpiry() == null) {
            throw new BadRequestException("No hay código pendiente para este usuario.");
        }
        if (user.getCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("El código de verificación ha expirado. Solicita uno nuevo.");
        }
        if (!user.getVerificationCode().equals(dto.getCode())) {
            throw new UnauthorizedException("El código ingresado no es válido.");
        }
        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new BadRequestException("El usuario ya está verificado.");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setCodeExpiry(null);
        userRepository.save(user);
    }

    // ------------------------------------------------------------
    // REENVIAR CÓDIGO DE VERIFICACIÓN
    // ------------------------------------------------------------
    @Transactional
    public void resendVerification(RequestResendDto dto) {
        var opt = userRepository.findByEmail(dto.getEmail());
        if (opt.isEmpty()) throw new NotFoundException("Usuario no encontrado.");

        if (!Boolean.FALSE.equals(opt.get().getVerified())) {
            throw new BadRequestException("El usuario ya está verificado.");
        }
        
        User user = opt.get();
        String code = generate6DigitCode();
        user.setVerificationCode(code);
        user.setCodeExpiry(LocalDateTime.now().plusMinutes(verificationCodeExpiryMinutes));
        userRepository.save(user);

        String subject = "Reenvío de código de verificación";
        String body = String.format("Tu nuevo código de verificación es: %s (válido por %d minutos)", code, verificationCodeExpiryMinutes);
        emailService.sendVerificationEmail(user.getEmail(), subject, body);
    }

    // ------------------------------------------------------------
    // MÉTODOS AUXILIARES
    // ------------------------------------------------------------
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(User user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    private String generate6DigitCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(900_000) + 100_000; // asegura 6 dígitos
        return String.valueOf(number);
    }
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
    public void resetPassword(Long userId, String code, String newPassword) {
        var optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) throw new NotFoundException("Usuario no encontrado.");

        User user = optUser.get();

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
    }
}
