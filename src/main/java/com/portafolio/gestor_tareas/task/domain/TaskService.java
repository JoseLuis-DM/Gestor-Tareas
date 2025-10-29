package com.portafolio.gestor_tareas.task.domain;

import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.BulkTaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {

    ResponseEntity<ApiResponseDTO<TaskDTO>> save(TaskDTO taskDTO);

    Task update(Task task, Long userId, UserDetails userDetails);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    void delete(Long id, UserDetails userDetails);

    void updateCompletionStatus(Long id, boolean complete, UserDetails userDetails);

    ResponseEntity<ApiResponseDTO<Map<String, Object>>> addTasksToUser(Long userId, List<Long> taskIds);

    ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> addTasksToUsers(BulkTaskDTO bulkTaskDTO);

    ResponseEntity<ApiResponseDTO<Map<String, Object>>> unassignTasksFromUser(Long userId, List<Long> taskIds);

    ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> unassignTasksFromUsers(BulkTaskDTO bulkTaskDTO);
}
