package com.portafolio.gestor_tareas.application.integration;

import com.portafolio.gestor_tareas.auth.application.AuthenticationService;
import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.auth.infrastructure.AuthenticationRequest;
import com.portafolio.gestor_tareas.auth.infrastructure.AuthenticationResponse;
import com.portafolio.gestor_tareas.auth.infrastructure.RegisterRequest;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationAndRefreshFlowTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private SpringUserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    private final String testEmail = "medinadomluis@gmail.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void fullAuthenticationFlow() {

        RegisterRequest request = new RegisterRequest(
                "Luis",
                "Medina",
                testEmail,
                "123456"
        );

        User user = userMapper.registerRequestToUser(request);

        authenticationService.register(user);

        UserEntity userEntity = userRepository.findByEmail(testEmail).orElseThrow();
        assertNotNull(user, "The user must exist in the database.");

        AuthenticationRequest authRequest = new AuthenticationRequest(
                testEmail,
                "123456"
        );
        AuthenticationResponse authResponse = authenticationService.authenticate(authRequest);

        assertNotNull(authResponse.getAccessToken(), "Access token must not be null");
        assertNotNull(authResponse.getRefreshToken(), "Refresh token must not be null");
        assertTrue(jwtService.isTokenValid(authResponse.getAccessToken(), userEntity), "New access token must be valid");

        String newAccessToken = refreshTokenService.generateNewAccessToken(authResponse.getRefreshToken());
        assertNotNull(newAccessToken, "New access token must not be null");
        assertTrue(jwtService.isTokenValid(newAccessToken, userEntity), "New access token must be valid");

        refreshTokenService.revokeByToken(authResponse.getRefreshToken());

        assertThrows(RuntimeException.class, () ->
                refreshTokenService.generateNewAccessToken(authResponse.getRefreshToken()), "The revoked refresh token should not generate a token.");
    }
}
