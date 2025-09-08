package com.portafolio.gestor_tareas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Standard API response wrapper")
public class ApiResponseDTO<T> {

    @Schema(example = "true")
    private boolean success;

    @Schema(example = "Operation completed successfully")
    private String message;

    private T data;

    @Schema(example = "2025-09-03T14:30:00")
    private LocalDateTime timestamp;
}
