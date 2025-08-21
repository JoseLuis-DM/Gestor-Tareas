package com.portafolio.gestor_tareas.task.domain;

import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Task {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private UserEntity user;
}
