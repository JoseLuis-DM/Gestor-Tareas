package com.portafolio.gestor_tareas.users.infrastructure.dto;

import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.users.domain.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private List<TaskEntity> task;
}
