package com.portafolio.gestor_tareas.auth.application;

import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.security.UserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final long refreshTokenDurationSec = 7 * 24 * 60 * 60; // 7 days

    public RefreshToken createRefreshToken(Long userId) {

        String token = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(token);

        Instant expiryDate = Instant.now().plusSeconds(refreshTokenDurationSec);

        RefreshToken refreshToken = new RefreshToken(hashedToken, userId, expiryDate, false);

        refreshTokenRepository.save(refreshToken);

        refreshToken.setToken(token);
        return refreshToken;
    }

    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findAll().stream()
                .filter(rt -> !rt.isRevoked() && passwordEncoder.matches(token, rt.getToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Refresh token not found or revoked"));

        if (refreshToken.getExpired().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
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

   public void revokeByUserId(Long userId) {
        refreshTokenRepository.findByUserId(userId)
                .forEach(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
   }

   public void revokeByToken(String token) {
        refreshTokenRepository.findAll().stream()
                .filter(rt -> passwordEncoder.matches(token, rt.getToken()))
                .findFirst()
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
   }
}
