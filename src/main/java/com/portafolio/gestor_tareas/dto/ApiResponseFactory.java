package com.portafolio.gestor_tareas.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ApiResponseFactory {

    public static <T>ResponseEntity<ApiResponseDTO<T>> success(T data, String message) {
        return ResponseEntity.ok()
                .body(
                        ApiResponseDTO.<T>builder()
                        .success(true)
                        .message(message)
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    public static <T>ResponseEntity<ApiResponseDTO<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponseDTO.<T>builder()
                                .success(true)
                                .message(message)
                                .data(data)
                                .timestamp(LocalDateTime.now())
                                .build());
    }

    public static <T>ResponseEntity<ApiResponseDTO<T>> noContent(String message) {
        return ResponseEntity.ok(
                ApiResponseDTO.<T>builder()
                        .success(true)
                        .message(message)
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    public static <T>ResponseEntity<ApiResponseDTO<T>> error(HttpStatus status, String message) {
        return ResponseEntity.ok(
                ApiResponseDTO.<T>builder()
                        .success(true)
                        .message(message)
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
