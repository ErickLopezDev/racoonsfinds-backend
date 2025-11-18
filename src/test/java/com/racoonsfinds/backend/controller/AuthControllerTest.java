package com.racoonsfinds.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
// import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.password.ForgotPasswordDto;
// import com.racoonsfinds.backend.dto.auth.password.ResetPasswordDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthServiceImpl authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); //  soporte para LocalDate

    }

    // ------------------------
    // REGISTER
    // ------------------------
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("UserTest");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setBirthDate(LocalDate.of(2000, 5, 10)); 

        doNothing().when(authService).register(any(RegisterRequestDto.class));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Usuario registrado. Revisa tu correo para confirmar."));

        verify(authService, times(1)).register(any(RegisterRequestDto.class));
    }



    // ------------------------
    // LOGIN
    // ------------------------
    // @Test
    // void shouldLoginSuccessfully() throws Exception {
    //     LoginRequestDto dto = new LoginRequestDto("test@example.com", "password123");
    //     AuthResponseDto resp = new AuthResponseDto(1L, "jwt-token", "refresh-token");

    //     when(authService.login(any(LoginRequestDto.class))).thenReturn(resp);

    //     mockMvc.perform(post("/api/auth/login")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(dto)))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.message").value("Login exitoso"))
    //         .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
    //         .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

    //     verify(authService).login(any(LoginRequestDto.class));
    // }

    // ------------------------
    // VERIFY ACCOUNT
    // ------------------------
    // @Test
    // void shouldVerifyAccountSuccessfully() throws Exception {
    //     VerifyCodeDto dto = new VerifyCodeDto();
    //     dto.setUserId(1L);
    //     dto.setCode("123123");

    //     doNothing().when(authService).verifyCode(any(VerifyCodeDto.class));

    //     mockMvc.perform(post("/api/auth/verify")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(dto)))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.message").value("Cuenta verificada correctamente"));

    //     verify(authService).verifyCode(any(VerifyCodeDto.class));
    // }

    // ------------------------
    // RESEND VERIFICATION
    // ------------------------
    // @Test
    // void shouldResendVerificationCodeSuccessfully() throws Exception {
    //     Long userId = 5L;
    //     doNothing().when(authService).resendVerification(userId);

    //     mockMvc.perform(post("/api/auth/resend", userId))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.message").value("Código de verificación reenviado correctamente"));

    //     verify(authService).resendVerification(userId);
    // }

    // ------------------------
    // FORGOT PASSWORD
    // ------------------------
    @Test
    void shouldSendForgotPasswordCodeSuccessfully() throws Exception {
        ForgotPasswordDto dto = new ForgotPasswordDto("test@example.com");
        doNothing().when(authService).forgotPassword(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Se ha enviado un código de recuperación a tu correo electrónico."));

        verify(authService).forgotPassword(anyString());
    }

    // // ------------------------
    // // RESET PASSWORD
    // // ------------------------
    // @Test
    // void shouldResetPasswordSuccessfully() throws Exception {
    //     ResetPasswordDto dto = new ResetPasswordDto();
    //     dto.setUserId(1L);
    //     dto.setCode("RESET123");
    //     dto.setNewPassword("newPassword123");

    //     doNothing().when(authService).resetPassword(anyLong(), anyString(), anyString());

    //     mockMvc.perform(post("/api/auth/reset-password")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(dto)))
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente."));

    //     verify(authService).resetPassword(anyLong(), anyString(), anyString());
    // }
}
//.