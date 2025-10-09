package com.portafolio.gestor_tareas.users.domain;

import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;

    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @Builder.Default
    private List<TaskEntity> task = new ArrayList<>();
}
