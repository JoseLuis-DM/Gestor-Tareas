package com.portafolio.gestor_tareas.auth.infrastructure;

import com.portafolio.gestor_tareas.auth.application.AuthenticationService;
import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    @Operation(summary = "Register a new user",
            description = "Create a new user in the system with the data provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationError"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/InvalidJson"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/UserAlreadyExists"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<AuthenticationResponse>> register(@RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - register new user: {}", request.getEmail());
        User user = userMapper.registerRequestToUser(request);
        AuthenticationResponse authResponse = authenticationService.register(user);
        log.info("User register successfully with id {}", user.getId());
        return ApiResponseFactory.created(authResponse, "User successfully registered");
    }

    @Operation(summary = "Authenticate user",
            description = "Generates a JWT token for the user if the credentials are correct.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully authenticate", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponseDTO<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("POST /api/auth/authenticate - authenticate user: {}", request.getEmail());
        AuthenticationResponse authResponse = authenticationService.authenticate(request);
        log.info("User authenticate successfully with email {}", request.getEmail());
        return ApiResponseFactory.success(authResponse, "User successfully authenticate");
    }

    @Operation(summary = "Register a new admin",
            description = "Creates a new admin in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationError"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/UserAlreadyExists"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponseDTO<AuthenticationResponse>> createAdmin(@RequestBody RegisterRequest request) {
        log.info("POST /api/auth/create-admin - create new admin: {}", request.getEmail());
        AuthenticationResponse authResponse = authenticationService.registerAdmin(request);
        log.info("Admin register successfully");
        return ApiResponseFactory.created(authResponse, "Admin successfully registered");
    }
}
