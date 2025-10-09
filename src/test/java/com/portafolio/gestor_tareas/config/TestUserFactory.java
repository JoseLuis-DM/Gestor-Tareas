package com.portafolio.gestor_tareas.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portafolio.gestor_tareas.auth.infrastructure.AuthenticationRequest;
import com.portafolio.gestor_tareas.auth.infrastructure.RegisterRequest;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
@Data
public class TestUserFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SpringUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor con @Autowired required = false para MockMvc
    @Autowired
    public TestUserFactory(
            @Autowired(required = false) MockMvc mockMvc,
            ObjectMapper objectMapper,
            SpringUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public TestUser createRegularUser() throws Exception {

        return registerAndAuthenticate(
                "usertest@test.com",
                "123456",
                "User",
                "Test",
                Role.USER,
                new ArrayList<>(),
                false,
                true
        );
    }

    public TestUser createRegularUserWithoutPermissions() throws Exception {

        return registerAndAuthenticate(
                "user@test.com",
                "123456",
                "Test",
                "User",
                Role.USER,
                new ArrayList<>(),
                false,
                false
        );
    }

    public TestUser createAdminUser() throws Exception {

        return registerAndAuthenticate(
                "admintest@test.com",
                "654321",
                "Admin",
                "Test",
                Role.ADMIN,
                new ArrayList<>(List.of("TASK_READ", "TASK_WRITE", "TASK_DELETE", "TASK_ASSIGN")),
         true,
                false
        );
    }

    private TestUser registerAndAuthenticate(String email,
                                             String password,
                                             String firstname,
                                             String lastname,
                                             Role role,
                                             List<String> permissions,
                                             boolean admin,
                                             boolean withPermission) throws Exception {

        if (userRepository.findByEmail(email).isEmpty()) {
            UserEntity user = new UserEntity();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstname(firstname);
            user.setLastname(lastname);
            user.setRole(role);

            if (withPermission) {
                user.setPermissions(new HashSet<>(Arrays.asList(
                        Permission.TASK_WRITE,
                        Permission.TASK_READ,
                        Permission.TASK_ASSIGN,
                        Permission.TASK_DELETE
                )));
            }

            userRepository.save(user);
        }

        AuthenticationRequest authRequest = new AuthenticationRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        String token = jsonNode.get("data").get("accessToken").asText();
        String bearerToken = "Bearer " + token;

        if (admin && permissions != null) {
            for (String permission : permissions) {
                mockMvc.perform(post("/api/users/email/{email}/permissions", email)
                                .header("Authorization", bearerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[\"" + permission + "\"]"))
                        .andReturn();
            }
        }

        return new TestUser(email, bearerToken);
    }

    public static class TestUser {

        private final String email;
        private final String token;

        public TestUser(String email, String token) {

            this.email = email;
            this.token = token;
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return token;
        }
    }
}
