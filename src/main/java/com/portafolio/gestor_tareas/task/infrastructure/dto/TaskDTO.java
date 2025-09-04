package com.portafolio.gestor_tareas.task.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskDTO(
        Long id,

        @NotBlank(message = "The title cannot be empty")
        @Size(max = 100, message = "The title cannot have more than 100 characters")
        String title,

        @Size(max = 300, message = "The description cannot have more than 300 characters")
        String description,
        boolean completed)
{ }
