package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.dto.auth.verify.VerifyCodeDto;
import com.racoonsfinds.backend.model.RefreshToken;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.security.JwtUtil;
import com.racoonsfinds.backend.service.int_.EmailService;
import com.racoonsfinds.backend.shared.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("UserTest");
        mockUser.setPassword("encodedPassword");
        mockUser.setVerified(true);
        mockUser.setFailedAttempts(0);
        mockUser.setLastLogin(LocalDateTime.now().minusMinutes(10));
    }

    // ------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------
    @Test
    void shouldLoginSuccessfully() {
        LoginRequestDto dto = new LoginRequestDto("test@example.com", "password123");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(dto.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

        RefreshToken mockRefreshToken = new RefreshToken(
            "refresh-token",
            mockUser,
            LocalDateTime.now().plusDays(7)
        );
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(mockRefreshToken);

        AuthResponseDto response = authService.login(dto);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository, atLeastOnce()).save(mockUser);
    }


    @Test
    void shouldThrowWhenUserNotFound() {
        LoginRequestDto dto = new LoginRequestDto("missing@example.com", "password123");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(dto));
    }

    @Test
    void shouldThrowWhenPasswordIncorrect() {
        LoginRequestDto dto = new LoginRequestDto("test@example.com", "wrong");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(dto));
    }

    @Test
    void shouldThrowWhenUserNotVerified() {
        mockUser.setVerified(false);
        LoginRequestDto dto = new LoginRequestDto("test@example.com", "password123");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> authService.login(dto));
    }

    // ------------------------------------------------------------
    // REGISTER
    // ------------------------------------------------------------
    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("UserTest");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        authService.register(dto);

        verify(emailService, times(1)).sendVerificationEmail(eq(dto.getEmail()), anyString(), contains("Tu código de verificación"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail("test@example.com");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class, () -> authService.register(dto));
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("UserTest");
        dto.setEmail("another@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class, () -> authService.register(dto));
    }

    // ------------------------------------------------------------
    // VERIFY CODE
    // ------------------------------------------------------------
    @Test
    void shouldVerifyCodeSuccessfully() {
        mockUser.setVerificationCode("123456");
        mockUser.setCodeExpiry(LocalDateTime.now().plusMinutes(10));

        VerifyCodeDto dto = new VerifyCodeDto();
        dto.setUserId(1L);
        dto.setCode("123456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        authService.verifyCode(dto);

        assertTrue(mockUser.getVerified());
        verify(userRepository).save(mockUser);
    }

    @Test
    void shouldThrowWhenVerificationCodeExpired() {
        mockUser.setVerificationCode("123456");
        mockUser.setCodeExpiry(LocalDateTime.now().minusMinutes(1));

        VerifyCodeDto dto = new VerifyCodeDto();
        dto.setUserId(1L);
        dto.setCode("123456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThrows(ForbiddenException.class, () -> authService.verifyCode(dto));
    }

    // ------------------------------------------------------------
    // RESEND VERIFICATION
    // ------------------------------------------------------------
    // @Test
    // void shouldResendVerificationCodeSuccessfully() {
    //     when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

    //     authService.resendVerification(1L);

    //     verify(emailService, times(1)).sendVerificationEmail(eq(mockUser.getEmail()), anyString(), contains("Tu nuevo código"));
    //     verify(userRepository).save(mockUser);
    // }

    // @Test
    // void shouldThrowWhenResendVerificationUserNotFound() {
    //     when(userRepository.findById(1L)).thenReturn(Optional.empty());
    //     assertThrows(NotFoundException.class, () -> authService.resendVerification(1L));
    // }

    // ------------------------------------------------------------
    // FORGOT PASSWORD
    // ------------------------------------------------------------
    @Test
    void shouldSendForgotPasswordEmail() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        authService.forgotPassword(mockUser.getEmail());

        verify(emailService).sendPasswordResetEmail(eq(mockUser.getEmail()), anyString(), contains("Recibimos una solicitud"));
    }

    @Test
    void shouldThrowWhenForgotPasswordUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> authService.forgotPassword("unknown@example.com"));
    }

    // ------------------------------------------------------------
    // RESET PASSWORD
    // ------------------------------------------------------------
    @Test
    void shouldResetPasswordSuccessfully() {
        mockUser.setVerificationCode("654321");
        mockUser.setCodeExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

        authService.resetPassword(1L, "654321", "newPassword");

        verify(userRepository).save(mockUser);
        assertEquals("newEncoded", mockUser.getPassword());
    }

    @Test
    void shouldThrowWhenResetPasswordUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> authService.resetPassword(1L, "123456", "pass"));
    }

    @Test
    void shouldThrowWhenResetPasswordCodeExpired() {
        mockUser.setVerificationCode("123456");
        mockUser.setCodeExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThrows(ForbiddenException.class, () -> authService.resetPassword(1L, "123456", "pass"));
    }

    @Test
    void shouldThrowWhenResetPasswordCodeInvalid() {
        mockUser.setVerificationCode("654321");
        mockUser.setCodeExpiry(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThrows(UnauthorizedException.class, () -> authService.resetPassword(1L, "wrong", "pass"));
    }
}