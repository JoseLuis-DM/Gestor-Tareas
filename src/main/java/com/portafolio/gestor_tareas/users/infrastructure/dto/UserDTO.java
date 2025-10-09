package com.portafolio.gestor_tareas.users.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    @NotBlank(message = "Firstname is required")
    @Size(max = 50, message = "The firstname cannot exceed 50 characters.")
    private String firstname;

    @NotBlank(message = "Lastname is required")
    @Size(max = 50, message = "The lastname cannot exceed 50 characters.")
    private String lastname;

    @NotBlank(message = "Email is required")
    @Size(message = "You must provide a valid email address.")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 6, message = "The password must be at least 6 characters long.")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    @JsonIgnore
    private List<TaskDTO> task = new ArrayList<>();

    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
