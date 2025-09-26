package com.portafolio.gestor_tareas.application.unit;

import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RefreshTokenServiceUnitTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SpringUserRepository springUserRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("123456");
    }

    @Test
    void shouldCreateRefreshToken() {

        when(passwordEncoder.encode(anyString())).thenReturn("hashedToken");
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(user.getId());

        assertNotNull(token.getToken(), "Must generate a plain (non-null) token");
        assertEquals(user.getId(), token.getUserId(), "The token must be linked to the correct user");
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void shouldValidateValidRefreshToken() {

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = "hashedToken";

        RefreshToken refreshToken = new RefreshToken(
                hashedToken,
                user.getId(),
                Instant.now().plusSeconds(36000),
                false
        );

        when(refreshTokenRepository.findAll()).thenReturn(List.of(refreshToken));
        when(passwordEncoder.matches(rawToken, hashedToken)).thenReturn(true);

        RefreshToken validated = refreshTokenService.validateRefreshToken(rawToken);

        assertEquals(user.getId(), validated.getUserId(), "The validated token must belong to the user");
    }


    @Test
    void shouldThrowExceptionWhenTokenExpired() {

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = "hashedToken";

        RefreshToken expiredToken = new RefreshToken(
                hashedToken,
                user.getId(),
                Instant.now().minusSeconds(10),
                false
        );

        when(refreshTokenRepository.findAll()).thenReturn(List.of(expiredToken));
        when(passwordEncoder.matches(rawToken, hashedToken)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> refreshTokenService.validateRefreshToken(rawToken));

        assertEquals("Refresh token expired", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    void shouldRevokeTokenByUserId() {

        RefreshToken token = new RefreshToken(
                "hashed",
                user.getId(),
                Instant.now().plusSeconds(36000),
                false
        );

        when(refreshTokenRepository.findByUserId(user.getId())).thenReturn(List.of(token));

        refreshTokenService.revokeByUserId(user.getId());

        assertTrue(token.isRevoked(), "The token must be marked as revoked");
        verify(refreshTokenRepository, times(1)).save(token);
    }
}