package com.racoonsfinds.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;
import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Collections;

import com.racoonsfinds.backend.service.RefreshTokenService;
import com.racoonsfinds.backend.model.RefreshToken;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.model.User;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        try {
            if (accessToken != null && jwtUtil.validateToken(accessToken)) {
                String subject = jwtUtil.getSubject(accessToken);
                setAuthentication(subject);
            } else if (accessToken != null && jwtUtil.isTokenExpired(accessToken)) {
                // intentar refresh
                String refreshTokenStr = request.getHeader("X-Refresh-Token");
                if (StringUtils.hasText(refreshTokenStr)) {
                    RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
                    if (refreshToken != null && refreshTokenService.isValid(refreshToken)) {
                        User user = refreshToken.getUser();
                        String newAccess = jwtUtil.generateToken(String.valueOf(user.getId()));

                        // devolver nuevo access en header para cliente
                        response.setHeader("X-New-Access-Token", newAccess);

                        // set authentication para la request actual
                        setAuthentication(String.valueOf(user.getId()));
                    } else {
                        // refresh inválido -> limpiar security
                        SecurityContextHolder.clearContext();
                    }
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String userId) {
        // para simplificar seteamos Authentication con username = userId; en producción
        // debes cargar roles/authorities desde DB o UserDetailsService
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Override
protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return path.matches("^/api/auth(/.*)?$")
        || path.startsWith("/swagger")
        || path.startsWith("/v3/api-docs")
        || path.equals("/error"); // importante para evitar 403 internos
}

}
