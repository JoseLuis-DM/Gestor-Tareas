package com.portafolio.gestor_tareas.application.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.auth.infrastructure.RefreshTokenController;
import com.portafolio.gestor_tareas.config.TestUserFactory;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RefreshTokenControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestUserFactory userFactory;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SpringUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TestUserFactory.TestUser regularUser;

    @BeforeEach
    void setUp() throws Exception {

        regularUser = userFactory.createRegularUser();
    }

    /*
        refreshToken (POST)
    */

    // Test to successfully refresh the user token
    @Test
    void refreshTokenUserSuccessfully() throws Exception {

        String refreshToken = regularUser.getRefreshToken();

        var request = new RefreshTokenController.RefreshTokenRequest();
        request.refreshToken = refreshToken;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token successfully renewed"));
    }

    // Test to make a refresh token but it is a bad request since the null refreshToken is sent
    @Test
    void shouldReturnBadRequestForInvalidData() throws Exception {

        String refreshToken = null;

        var request = new RefreshTokenController.RefreshTokenRequest();
        request.refreshToken = refreshToken;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test to test when a token is sent that does not exist
    @Test
    void shouldReturnNotFoundForNoExistRefreshToken() throws Exception {

        String refreshToken = regularUser.getRefreshToken() + "notfound";

        var request = new RefreshTokenController.RefreshTokenRequest();
        request.refreshToken = refreshToken;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // Test where the refresh token was revoked
    @Test
    void refreshTokenRevokedShouldReturn403() throws Exception {

        String tokenValue = "revoked-refresh-token";

        Long userId = userRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        RefreshToken token = new RefreshToken(
                passwordEncoder.encode(tokenValue),
                userId,
                Instant.now().plus(1, ChronoUnit.DAYS),
                true
        );
        refreshTokenRepository.save(token);

        var request = new RefreshTokenController.RefreshTokenRequest();
        request.refreshToken = tokenValue;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // Test where the refresh token was expired
    @Test
    void refreshTokenExpiredShouldReturn401() throws Exception {

        String tokenValue = "expired-refresh-token";

        Long userId = userRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        RefreshToken token = new RefreshToken(
                tokenValue,
                userId,
                Instant.now().minus(1, ChronoUnit.HOURS),
                false
        );
        refreshTokenRepository.save(token);

        var request = new RefreshTokenController.RefreshTokenRequest();
        request.refreshToken = tokenValue;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    /*
        logout (POST)
    */

    // Test where a logout is successfully made
    @Test
    void logoutUserSuccessfully() throws Exception {

        String refreshToken = regularUser.getRefreshToken();

        var request = new RefreshTokenController.RefreshTokenRequest();
        request.refreshToken = refreshToken;
        System.out.println(refreshToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    // Test to test when a token is sent that does not exist
    @Test
    void shouldReturnNotFoundForNoExistLogout() throws Exception {

        var request = new RefreshTokenController.RefreshTokenRequest();
        request.refreshToken = "notfound";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /*
        logoutAll (POST)
    */

    // Test where a global logout is successfully made
    @Test
    void globalLogoutUserSuccessfully() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout-all")
                        .with(user(regularUser.getEmail()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successful global logout"));
    }

}
