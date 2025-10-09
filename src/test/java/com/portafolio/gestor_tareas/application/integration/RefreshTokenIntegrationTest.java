package com.portafolio.gestor_tareas.application.integration;

import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RefreshTokenIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SpringUserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user = new UserEntity();
        user.setFirstname("Integration");
        user.setLastname("Test");
        user.setEmail("integration@example.com");
        user.setPassword("123456");
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    @Test
    void testCreateAndValidateRefreshToken() {

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        assertNotNull(refreshToken, "Refresh token must be generated");
        assertNotNull(refreshToken.getExpired(), "It must have an expiration date");
        assertFalse(refreshToken.isRevoked(), "It must not be revoked at the beginning");

        RefreshToken validated = refreshTokenService.validateRefreshToken(refreshToken.getToken());
        assertEquals(user.getId(), validated.getUserId(), "UserId must match");
    }

    @Test
    void testRevokeByUserId() {

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        refreshTokenService.revokeByUserId(user.getId());

        RefreshToken revoked = refreshTokenRepository.findById(refreshToken.getId()).orElseThrow();
        assertTrue(revoked.isRevoked());
    }

    @Test
    void testRevokeByToken() {

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        refreshTokenService.revokeByToken(refreshToken.getToken());

        RefreshToken revoked = refreshTokenRepository.findById(refreshToken.getId()).orElseThrow();
        assertTrue(revoked.isRevoked());
    }

    @Test
    void testExpiredTokenThrowsException() {

        RefreshToken expiredToken = new RefreshToken(
                "hashedToken",
                user.getId(),
                Instant.now().minusSeconds(60),
                false
        );

        refreshTokenRepository.save(expiredToken);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.validateRefreshToken("expiredToken");
        });

        assertEquals("Refresh token not found or revoked", exception.getMessage());
    }
}