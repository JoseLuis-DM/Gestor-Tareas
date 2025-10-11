package com.portafolio.gestor_tareas.auth.infrastructure;

import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and token management")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final SpringUserRepository springUserRepository;

    @Schema(description = "Request to refresh the access token")
    public static class RefreshTokenRequest {
        @Schema(description = "Refresh valid token", example = "2f3e7c82-3c76-4f56-9b8f-17af91d45d8a")
        public String refreshToken;
    }

    @Schema(description = "Response to refreshing the access token")
    public static class RefreshTokenResponse {
        @Schema(description = "New access token generated")
        public String accessToken;

        @Schema(description = "Refresh valid token")
        public String refreshToken;

        public RefreshTokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Operation(summary = "Renew access token with refresh token",
            description = "Generates a new access token from a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token successfully renewed"),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponseDTO<RefreshTokenResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {

        String newAccessToken = refreshTokenService.generateNewAccessToken(request.refreshToken);

        RefreshTokenResponse response = new RefreshTokenResponse(newAccessToken, request.refreshToken);

        return ApiResponseFactory.success(response, "Token successfully renewed");
    }

    @Operation(summary = "Sign out of the current device",
            description = "Deletes only the current refresh token (logout on a device).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<RefreshTokenResponse>> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeByToken(request.refreshToken);
        return ApiResponseFactory.success(null, "Logout successful");
    }

    @Operation(summary = "Sign out on all devices",
            description = "Deletes all refresh tokens associated with the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful global logout"),
            @ApiResponse(responseCode = "404", ref = "User not found")
    })
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponseDTO<RefreshTokenResponse>> logoutAll(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity user = springUserRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new NotFoundException("User not found"));

        refreshTokenService.revokeByUserId(user.getId());
        return ApiResponseFactory.success(null, "Successful global logout");
    }
}