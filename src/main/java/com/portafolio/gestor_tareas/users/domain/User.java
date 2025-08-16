package com.portafolio.gestor_tareas.users.domain;

import com.portafolio.gestor_tareas.tasks.infrastructure.entity.TasksEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class User {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
    private List<TasksEntity> task;
}
