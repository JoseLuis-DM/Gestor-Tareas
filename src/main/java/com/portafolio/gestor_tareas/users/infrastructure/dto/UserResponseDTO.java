package com.portafolio.gestor_tareas.users.infrastructure.dto;

import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;

    private String firstname;

    private String lastname;

    private String email;

    private Role role;

    private Set<Permission> permissions;
}
