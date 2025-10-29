package com.portafolio.gestor_tareas.task.infrastructure;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.BulkTaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface TaskController {

    ResponseEntity<ApiResponseDTO<TaskDTO>> register(TaskDTO taskDTO);

    ResponseEntity<ApiResponseDTO<TaskDTO>> update(TaskDTO taskDTO, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<TaskDTO>> findById(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<List<TaskDTO>>> findAll(UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<Void>> deleteById(Long id, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<Void>> updateCompletionStatus(Long id, boolean completed, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<Map<String, Object>>> addTasksToUser(Long userId, List<Long> taskIds);

    ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> addTasksToUsers(BulkTaskDTO bulkTaskDTO);

    ResponseEntity<ApiResponseDTO<Map<String, Object>>> unassignTasksFromUser(Long userId, List<Long> taskIds);

    ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> unassignTasksFromUsers(BulkTaskDTO bulkTaskDTO);
}
