package com.portafolio.gestor_tareas.auth.infrastructure;

import com.portafolio.gestor_tareas.auth.application.AuthenticationService;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and token management")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    @Operation(summary = "Register a new user",
            description = "Create a new user in the system with the data provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", ref = "BadRequest", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        User user = userMapper.registerRequestToUser(request);

        return ResponseEntity.ok(authenticationService.register(user));
    }

    @Operation(summary = "Authenticate user",
            description = "Generates a JWT token for the user if the credentials are correct.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered", content = @Content),
            @ApiResponse(responseCode = "401", ref = "Unauthorized", content = @Content)
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-admin")
    public ResponseEntity<AuthenticationResponse> createAdmin(@RequestBody RegisterRequest request) {
        User user = authenticationService.registerAdmin(request);
        String token = authenticationService.generateToken(user);
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }
}
