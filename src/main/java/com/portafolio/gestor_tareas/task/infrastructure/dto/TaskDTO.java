package com.portafolio.gestor_tareas.task.infrastructure.dto;

public record TaskDTO(
        Long id,
        String title,
        String description,
        boolean completed)
{ }
