package com.portafolio.gestor_tareas.task.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface TaskController {

    ResponseEntity<ApiResponseDTO<TaskDTO>> register(TaskDTO taskDTO);

    ResponseEntity<ApiResponseDTO<TaskDTO>> update(TaskDTO taskDTO, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<TaskDTO>> findById(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<List<TaskDTO>>> findAll(UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<Void>> deleteById(Long id, UserDetails userDetails);
}
