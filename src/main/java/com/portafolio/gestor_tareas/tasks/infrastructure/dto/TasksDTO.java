package com.portafolio.gestor_tareas.tasks.infrastructure.dto;

public record TasksDTO(
        Long id,
        String title,
        String description,
        boolean completed)
{ }
