package com.portafolio.gestor_tareas.auth.application;

import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDurationSec = 7 * 24 * 60 * 60; // 7 days

    public RefreshToken createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenDurationSec);

        RefreshToken refreshToken = new RefreshToken(token, userId, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> verifyExpiration(RefreshToken token) {
        if (token.getExpired().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            return Optional.empty();
        }
        return Optional.of(token);
   }

   public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
   }

   public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
   }
}
