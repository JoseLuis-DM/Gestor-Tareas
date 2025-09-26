package com.portafolio.gestor_tareas.application.unit;

import com.portafolio.gestor_tareas.auth.application.AuthenticationService;
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
public class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private SpringUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserMapper userMapper;

    private final String testEmail = "medinadomluis@gmail.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest(
                "Luis",
                "Medina",
                testEmail,
                "123456"
        );

        User user = userMapper.registerRequestToUser(request);

        authenticationService.register(user);

        UserEntity userEntity = userRepository.findByEmail(testEmail).orElseThrow();
        assertEquals("Luis", user.getFirstname(), "The name must match");
        assertEquals("Medina", user.getLastname(), "The last name must match");

        assertTrue(passwordEncoder.matches("123456", user.getPassword()), "The password must match after encryption");
    }

    @Test
    void testAuthenticateReturnsToken() {

        RegisterRequest request = new RegisterRequest(
                "Luis",
                "Medina",
                testEmail,
                "123456"
        );

        User user = userMapper.registerRequestToUser(request);

        authenticationService.register(user);

        AuthenticationRequest authRequest = new AuthenticationRequest(
                testEmail,
                "123456"
        );

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertNotNull(response.getAccessToken(), "The access token must not be null");
        assertNotNull(response.getRefreshToken(), "The refresh token must not be null");

        UserEntity userEntity = userRepository.findByEmail(testEmail).orElseThrow();
        assertTrue(jwtService.isTokenValid(response.getAccessToken(), userEntity), "The token must be valid for the user");
    }

    @Test
    void testAuthenticateWithWrongPassword() {

        RegisterRequest request = new RegisterRequest(
                "Luis",
                "Medina",
                testEmail,
                "123456"
        );

        User user = userMapper.registerRequestToUser(request);

        authenticationService.register(user);

        AuthenticationRequest authRequest = new AuthenticationRequest(
                testEmail,
                "wrongPassword"
        );

        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(authRequest), "Should throw exception for incorrect password");
    }
}
