package com.portafolio.gestor_tareas.application.unit;

import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setFirstname("testUser");
        user.setEmail("test@example.com");
        user.setPassword("123456");
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token, "The token must not be null");

        String userName = jwtService.extractUsername(token);
        assertEquals("test@example.com", userName, "The extracted username must match");

        boolean isValid = jwtService.isTokenValid(token, user);
        assertTrue(isValid, "The token should be valid for the user");
    }

    @Test
    void shouldExtractUsernameFromToken() {

        String userName = "test@example.com";

        String token = jwtService.generateToken(user);

        String extractUsername = jwtService.extractUsername(token);

        assertEquals(userName, extractUsername, "The extracted username must match");
    }

    @Test
    void shouldValidateValidToken() {

        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token, user);

        assertTrue(isValid, "The token should be valid");
    }

    @Test
    void shouldInvalidateTamperedToken() {

        String token = jwtService.generateToken(user);

        String tamperedToken = token.substring(0, token.length() - 2) + "aa";

        boolean isValid = jwtService.isTokenValid(tamperedToken, user);

        assertFalse(isValid, "The altered token should not be valid");
    }
}