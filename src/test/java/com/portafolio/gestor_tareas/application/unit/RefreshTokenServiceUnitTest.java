package com.portafolio.gestor_tareas.application.unit;

import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.exception.domain.RefreshTokenExpiredException;
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

class RefreshTokenServiceUnitTest {

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

        Long userId = user.getId();
        String hashedToken = "hashed-token";

        when(passwordEncoder.encode(anyString())).thenReturn(hashedToken);
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        RefreshToken created = refreshTokenService.createRefreshToken(userId);

        assertNotNull(created, "The refresh token object should not be null");
        assertNotNull(created.getToken(), "The plain token must be set for returning to the client");
        assertEquals(userId, created.getUserId(), "The token must belong to the expected user");
        assertFalse(created.isRevoked(), "A new token should not be revoked by default");
        assertTrue(created.getExpired().isAfter(Instant.now()), "The expiry date should be in the future");

        verify(passwordEncoder, times(1)).encode(anyString());
        verify(refreshTokenRepository, times(1)).saveAndFlush(any(RefreshToken.class));
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

        RefreshTokenExpiredException exception = assertThrows(
                RefreshTokenExpiredException.class,
                () -> refreshTokenService.validateRefreshToken(rawToken)
        );

        assertEquals("RefreshToken expired. Refresh token expired", exception.getMessage());
        assertTrue(expiredToken.isRevoked(), "Expired token must be marked as revoked");
        verify(refreshTokenRepository, times(1)).save(expiredToken);
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