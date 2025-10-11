package com.portafolio.gestor_tareas.auth.application;

import com.portafolio.gestor_tareas.auth.domain.RefreshToken;
import com.portafolio.gestor_tareas.auth.domain.RefreshTokenRepository;
import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.exception.domain.*;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final SpringUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshTokenDurationSec = 7 * 24 * 60 * 60; // 7 days

    public RefreshToken createRefreshToken(Long userId) {

        String token = UUID.randomUUID().toString();
        String hashedToken = passwordEncoder.encode(token);

        Instant expiryDate = Instant.now().plusSeconds(refreshTokenDurationSec);

        RefreshToken refreshToken = new RefreshToken(hashedToken, userId, expiryDate, false);
        refreshTokenRepository.saveAndFlush(refreshToken);

        RefreshToken response = new RefreshToken();
        response.setId(refreshToken.getId());
        response.setToken(token);
        response.setUserId(userId);
        response.setExpired(expiryDate);
        response.setRevoked(false);

        return response;
    }

    public RefreshToken validateRefreshToken(String token) {

        if (token == null || token.isEmpty()) {
            throw new BadRequestException("The refresh token cannot be null or empty");
        }

        RefreshToken refreshToken = refreshTokenRepository.findAll().stream()
                .filter(rt -> passwordEncoder.matches(token, rt.getToken()) || rt.getToken().equals(token))
                .findFirst()
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenRevokedException("Token revoked");
        }

        if (refreshToken.getExpired().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new RefreshTokenExpiredException("Refresh token expired");
        }

        return refreshToken;
    }

    public String generateNewAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = validateRefreshToken(refreshTokenStr);

        UserEntity userEntity = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return jwtService.generateToken(userEntity);
    }

   public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
   }

   public void revokeByUserId(Long userId) {
       List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);

       if (tokens.isEmpty()) {
           throw new RefreshTokenNotFoundException("No refresh tokens found for this user");
       }

       for (RefreshToken rt : tokens) {
           if (!rt.isRevoked()) {
               if (rt.getExpired().isBefore(Instant.now())) {
                   rt.setRevoked(true);
               } else {
                   rt.setRevoked(true);
               }
               refreshTokenRepository.save(rt);
           }
       }
   }

   public void revokeByToken(String token) {

       List<RefreshToken> allTokens = refreshTokenRepository.findAll();

       RefreshToken match = allTokens.stream()
               .filter(rt ->
                       passwordEncoder.matches(token, rt.getToken()) || rt.getToken().equals(token)
               )
               .findFirst()
               .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

       if (match.isRevoked()) {

           throw new RefreshTokenRevokedException("Token already revoked");
       }

       match.setRevoked(true);
       refreshTokenRepository.save(match);
   }
}
