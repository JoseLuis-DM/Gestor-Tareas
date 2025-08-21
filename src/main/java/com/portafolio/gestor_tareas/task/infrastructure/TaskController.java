package com.portafolio.gestor_tareas.task.infrastructure;

import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TaskController {

    ResponseEntity<TaskDTO> register(TaskDTO taskDTO);

    ResponseEntity<TaskDTO> update(TaskDTO taskDTO);

    ResponseEntity<TaskDTO> findById(Long id);

    ResponseEntity<List<TaskDTO>> findAll();

    ResponseEntity<Void> deleteById(Long id);
}
