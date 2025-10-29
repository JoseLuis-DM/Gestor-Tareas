package com.portafolio.gestor_tareas.task.domain;

import com.portafolio.gestor_tareas.users.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Builder
public class Task {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private User user;
}
