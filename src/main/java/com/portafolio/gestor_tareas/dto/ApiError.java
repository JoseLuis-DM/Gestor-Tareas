package com.portafolio.gestor_tareas.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;
    private List<String> errors;
}
