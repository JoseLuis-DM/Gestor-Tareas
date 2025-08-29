package com.portafolio.gestor_tareas.exception.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;
    private List<String> errors;

    public ApiError(int status, String error, String path, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.path = path;
        this.timestamp = timestamp;
    }

    public ApiError(int status, String error, String path) {
        this.status = status;
        this.error = error;
        this.path = path;
    }
}
