package com.portafolio.gestor_tareas.auth.application;

import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.security.UserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final long refreshTokenDurationSec = 7 * 24 * 60 * 60; // 7 days

    public RefreshToken createRefreshToken(Long userId) {

        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenDurationSec);

        RefreshToken refreshToken = new RefreshToken(token, userId, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpired().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public String generateNewAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = validateRefreshToken(refreshTokenStr);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetailsAdapter userDetails = new UserDetailsAdapter(user);

        return jwtService.generateToken(userDetails);
    }

   public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
   }

   public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
   }

   public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
   }
}
