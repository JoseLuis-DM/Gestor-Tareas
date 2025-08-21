package com.portafolio.gestor_tareas.task.infrastructure;

import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskService;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/task")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Task", description = "The Task API. Contains all operation on tasks")
@RequiredArgsConstructor
public class TaskControllerImpl implements TaskController{

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @Operation(summary = "Register a new task", description = "Creates a new task in the system")
    @PostMapping
    public ResponseEntity<TaskDTO> register(@Valid @RequestBody TaskDTO taskDTO) {
        Task task = taskMapper.taskDTOToTask(taskDTO);

        Task register = taskService.save(task);

        TaskDTO registerDTO = taskMapper.taskToTaskDTO(register);

        return ResponseEntity.status(HttpStatus.CREATED).body(registerDTO);
    }

    @Operation(summary = "Update an existing task", description = "Updates task details")
    @PutMapping
    public ResponseEntity<TaskDTO> update(@Valid @RequestBody TaskDTO taskDTO) {

        Task task = taskMapper.taskDTOToTask(taskDTO);

        Task update = taskService.update(task);

        TaskDTO updateDTO = taskMapper.taskToTaskDTO(update);

        return ResponseEntity.ok(updateDTO);
    }

    @Operation(summary = "Find task by ID", description = "Retrieve a task by their ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> findById(@PathVariable Long id) throws NotFoundException {

        Task task = taskService.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        TaskDTO taskDTO = taskMapper.taskToTaskDTO(task);
        return ResponseEntity.ok(taskDTO);
    }

    @Operation(summary = "List all tasks", description = "Returns a list of all tasks")
    @GetMapping
    public ResponseEntity<List<TaskDTO>> findAll() {

        List<TaskDTO> taskDTO = taskService.findAll()
                .stream().map(taskMapper::taskToTaskDTO).toList();

        return ResponseEntity.ok(taskDTO);
    }

    @Operation(summary = "Delete task by ID", description = "Deletes a task by their ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        taskService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
