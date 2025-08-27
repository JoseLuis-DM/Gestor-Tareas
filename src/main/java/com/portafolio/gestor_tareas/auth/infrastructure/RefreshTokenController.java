package com.portafolio.gestor_tareas.auth.infrastructure;

import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.security.UserDetailsAdapter;
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

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication and token management")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

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
            @ApiResponse(responseCode = "400", description = "Refresh token invalid or expired")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {

        try {
            String newAccessToken = refreshTokenService.generateNewAccessToken(request.refreshToken);
            return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, request.refreshToken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Sign out of the current device",
            description = "Deletes only the current refresh token (logout on a device).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful "),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logout successful (single device)"));
    }

    @Operation(summary = "Sign out on all devices",
            description = "Deletes all refresh tokens associated with the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful global logout"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@AuthenticationPrincipal UserDetailsAdapter user) {
        refreshTokenService.deleteByUserId(user.getUser().getId());
        return ResponseEntity.ok(Map.of("message", "Logged out from all devices"));
    }
}
