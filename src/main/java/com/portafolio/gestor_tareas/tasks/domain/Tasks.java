package com.portafolio.gestor_tareas.tasks.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Tasks {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
}
