package com.portafolio.gestor_tareas.config.infrastructure;

import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final SpringUserRepository springUserRepository;

    public SecurityUtils(SpringUserRepository springUserRepository) {
        this.springUserRepository = springUserRepository;
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserEntity userEntity) {
            return userEntity.getId();
        }

        throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
    }

    public UserEntity getCurrentUserEntity() {
        Long userId = getCurrentUserId();
        return springUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
