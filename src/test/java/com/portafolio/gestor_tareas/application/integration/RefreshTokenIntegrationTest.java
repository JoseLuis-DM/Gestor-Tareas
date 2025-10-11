package com.portafolio.gestor_tareas.application.integration;

import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.exception.domain.RefreshTokenExpiredException;
import com.portafolio.gestor_tareas.exception.domain.RefreshTokenNotFoundException;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SpringUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        userRepository.saveAndFlush(user);
    }

    @Test
    void testCreateAndValidateRefreshToken() {

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        refreshTokenRepository.flush();

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
        refreshTokenRepository.flush();

        System.out.println(refreshToken.getToken());
        System.out.println(refreshToken.getId());
        refreshTokenService.revokeByToken(refreshToken.getToken());

        System.out.println(refreshToken.getId());
        RefreshToken revoked = refreshTokenRepository.findById(refreshToken.getId())
                .orElseThrow(() -> new RefreshTokenNotFoundException("Token is not found in DB"));

        assertTrue(revoked.isRevoked(), "The token should be revoked");
    }

    @Test
    void testExpiredTokenThrowsException() {

        String tokenValue = "expiredToken";

        RefreshToken expiredToken = new RefreshToken(
                passwordEncoder.encode(tokenValue),
                user.getId(),
                Instant.now().minusSeconds(60),
                false
        );

        refreshTokenRepository.save(expiredToken);

        RefreshTokenExpiredException exception = assertThrows(RefreshTokenExpiredException.class, () -> {
            refreshTokenService.validateRefreshToken(tokenValue);
        });

        assertEquals("RefreshToken expired. Refresh token expired", exception.getMessage());
    }
}