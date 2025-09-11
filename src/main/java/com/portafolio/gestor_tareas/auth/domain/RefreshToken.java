package com.portafolio.gestor_tareas.auth.domain;

import com.portafolio.gestor_tareas.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
@Builder
public class RefreshToken extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant expired;

    @Column(nullable = false)
    private boolean revoked;

    public RefreshToken(String token, Long userId, Instant expired, boolean revoked) {
        this.token = token;
        this.userId = userId;
        this.expired = expired;
        this.revoked = revoked;
    }
}
