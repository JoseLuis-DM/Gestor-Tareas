package com.portafolio.gestor_tareas.task.domain;

import com.portafolio.gestor_tareas.task.infrastructure.dto.BulkTaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface TaskService {

    TaskDTO save(TaskDTO taskDTO);

    TaskDTO update(TaskDTO taskDTO, Long userId, UserDetails userDetails);

    TaskDTO findById(Long id, UserDetails userDetails);

    List<TaskDTO> findAll(Long id, UserDetails userDetails);

    void delete(Long id, UserDetails userDetails);

    void updateCompletionStatus(Long id, boolean complete, UserDetails userDetails);

    Map<String, List<String>> addTasksToUser(Long userId, List<Long> taskIds);

    Map<String, List<String>> addTasksToUsers(BulkTaskDTO bulkTaskDTO);

    Map<String, List<String>> unassignTasksFromUser(Long userId, List<Long> taskIds);

    Map<String, List<String>> unassignTasksFromUsers(BulkTaskDTO bulkTaskDTO);
}
