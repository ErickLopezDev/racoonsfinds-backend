package com.racoonsfinds.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.security.JwtAuthenticationFilter;
import com.racoonsfinds.backend.security.JwtUtil;
import com.racoonsfinds.backend.service.RefreshTokenService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public SecurityConfig(JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, refreshTokenService, userRepository);

    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/error").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}



    // ✅ Bean necesario para autenticación manual en el servicio de login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
