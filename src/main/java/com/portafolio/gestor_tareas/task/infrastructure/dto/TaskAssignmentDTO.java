package com.portafolio.gestor_tareas.task.infrastructure.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TaskAssignmentDTO(

        @NotNull(message = "userId cannot be null")
        Long userId,

        @NotEmpty(message = "taskId cannot be empty")
        List<Long> taskIds
) {
}
