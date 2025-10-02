package com.racoonsfinds.backend.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.racoonsfinds.backend.dto.auth.AuthResponseDto;
import com.racoonsfinds.backend.dto.auth.login.LoginRequestDto;
import com.racoonsfinds.backend.dto.auth.login.OAuth2RequestDto;
import com.racoonsfinds.backend.dto.auth.register.RegisterRequestDto;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.security.JwtService;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Servicio que genera tokens JWT

    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = MapperUtil.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        String access = jwtService.generateAccessToken(saved);
        String refresh = jwtService.generateRefreshToken(saved);

        return new AuthResponseDto(access, refresh);
    }

    public AuthResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return new AuthResponseDto(access, refresh);
    }

    public AuthResponseDto oauth2Login(OAuth2RequestDto dto) {

        User user = userRepository.findByEmail(dto.email())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(dto.email());
                    newUser.setPassword("OAUTH2"); // no se usa
                    newUser.setVerified(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return new AuthResponseDto(access, refresh);
    }
}
