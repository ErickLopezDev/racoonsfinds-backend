package com.racoonsfinds.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.racoonsfinds.backend.model.RefreshToken;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        
        // Simulamos el valor que normalmente inyecta Spring:
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMinutes", 30L);
    }

    @Test
    void createRefreshToken_ShouldGenerateNewToken() {
        User user = new User();
        user.setId(1L);

        RefreshToken savedToken = new RefreshToken("123", user, LocalDateTime.now().plusMinutes(30));

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        verify(refreshTokenRepository, times(1)).deleteByUser(user);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        assertNotNull(result);
        assertEquals(user, result.getUser());
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTokenNotExpired() {
        RefreshToken token = new RefreshToken("abc", new User(), LocalDateTime.now().plusMinutes(5));
        assertTrue(refreshTokenService.isValid(token));
    }

    @Test
    void isValid_ShouldReturnFalse_WhenTokenExpired() {
        RefreshToken token = new RefreshToken("abc", new User(), LocalDateTime.now().minusMinutes(1));
        assertFalse(refreshTokenService.isValid(token));
    }

    @Test
    void findByToken_ShouldReturnToken_WhenExists() {
        RefreshToken token = new RefreshToken("xyz", new User(), LocalDateTime.now().plusMinutes(30));
        when(refreshTokenRepository.findByToken("xyz")).thenReturn(Optional.of(token));

        RefreshToken found = refreshTokenService.findByToken("xyz");

        assertNotNull(found);
        assertEquals("xyz", found.getToken());
    }

    @Test
    void findByToken_ShouldReturnNull_WhenNotExists() {
        when(refreshTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        assertNull(refreshTokenService.findByToken("nope"));
    }

    @Test
    void deleteByUser_ShouldInvokeRepositoryDelete() {
        User user = new User();
        refreshTokenService.deleteByUser(user);
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }
}