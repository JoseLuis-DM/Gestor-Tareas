package com.portafolio.gestor_tareas.config.infrastructure;

import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SpringUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {
            UserEntity admin = new UserEntity();
            admin.setFirstname("admin");
            admin.setEmail("admin@local");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setPermissions(Set.of(
                    Permission.TASK_READ,
                    Permission.TASK_WRITE,
                    Permission.TASK_ASSIGN,
                    Permission.TASK_DELETE,
                    Permission.TASK_UNASSIGN
            ));

            userRepository.save(admin);
        }
    }
}
