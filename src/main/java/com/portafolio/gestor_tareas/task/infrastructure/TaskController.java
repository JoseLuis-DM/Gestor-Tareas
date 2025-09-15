package com.portafolio.gestor_tareas.task.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TaskController {

    ResponseEntity<ApiResponseDTO<TaskDTO>> register(TaskDTO taskDTO);

    ResponseEntity<ApiResponseDTO<TaskDTO>> update(TaskDTO taskDTO);

    ResponseEntity<ApiResponseDTO<TaskDTO>> findById(Long id);

    ResponseEntity<ApiResponseDTO<List<TaskDTO>>> findAll();

    ResponseEntity<ApiResponseDTO<Void>> deleteById(Long id);
}
