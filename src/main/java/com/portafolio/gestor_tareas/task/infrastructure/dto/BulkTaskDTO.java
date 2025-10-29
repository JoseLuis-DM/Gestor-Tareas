package com.portafolio.gestor_tareas.task.infrastructure.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkTaskDTO(

        @Valid
        @NotEmpty(message = "assignments cannot be empty")
        List<TaskAssignmentDTO> assignments
) {
}
