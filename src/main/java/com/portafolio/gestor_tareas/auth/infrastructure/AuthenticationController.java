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
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationError"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/InvalidJson"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/UserAlreadyExists"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<AuthenticationResponse>> register(@RequestBody RegisterRequest request) {
        User user = userMapper.registerRequestToUser(request);
        AuthenticationResponse authResponse = authenticationService.register(user);
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

        AuthenticationResponse authResponse = authenticationService.authenticate(request);

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

        AuthenticationResponse authResponse = authenticationService.registerAdmin(request);

        return ApiResponseFactory.created(authResponse, "Admin successfully registered");
    }
}
