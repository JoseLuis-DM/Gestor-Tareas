package com.portafolio.gestor_tareas.users.infrastructure.dto;

import com.portafolio.gestor_tareas.users.domain.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithPermissionsDTO {

    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private Set<Permission> permissions;
}
