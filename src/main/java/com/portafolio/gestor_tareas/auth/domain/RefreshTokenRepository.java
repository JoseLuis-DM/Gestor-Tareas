package com.portafolio.gestor_tareas.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void delete(RefreshToken refreshToken);

    List<RefreshToken> findByUserId(Long userId);
}
