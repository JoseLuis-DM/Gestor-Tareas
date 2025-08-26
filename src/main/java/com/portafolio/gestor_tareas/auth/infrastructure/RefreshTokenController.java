package com.portafolio.gestor_tareas.auth.infrastructure;

import com.portafolio.gestor_tareas.auth.application.RefreshTokenService;
import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.security.UserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public static class RefreshTokenRequest {
        public String refreshToken;
    }

    public static class RefreshTokenResponse {
        public String accessToken;
        public String refreshToken;

        public RefreshTokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {

        Optional<RefreshToken> optionalToken = refreshTokenService.findByToken(request.refreshToken);

        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }

        RefreshToken refreshToken = optionalToken.get();

        Optional<RefreshToken> validToken = refreshTokenService.verifyExpiration(refreshToken);
        if (validToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetailsAdapter userDetails = new UserDetailsAdapter(user);

        String newAccesToken =  jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new RefreshTokenResponse(newAccesToken, refreshToken.getToken()));
    }
}
