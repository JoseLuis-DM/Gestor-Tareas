package com.portafolio.gestor_tareas.task.infrastructure;

import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.config.infrastructure.SecurityUtils;
import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskService;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Task", description = "The Task API. Contains all operation on tasks")
@RequiredArgsConstructor
public class TaskControllerImpl implements TaskController{

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final SecurityUtils securityUtils;
    private final JwtService jwtService;

    @Operation(summary = "Register a new task",
            description = "Creates a new task in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", ref = "BadRequest", content = @Content)
    })
    @PreAuthorize("hasAuthority('TASK_WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<TaskDTO>> register(@Valid @RequestBody TaskDTO taskDTO) {
        Long userId = securityUtils.getCurrentUserId();
        Task task = taskMapper.taskDTOToTask(taskDTO);
        Task saved = taskService.save(task, userId);

        return ApiResponseFactory.created(taskMapper.taskToTaskDTO(saved), "Task created successfully");
    }

    @Operation(summary = "Update an existing task",
            description = "Updates task details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", ref = "BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasAuthority('TASK_WRITE')")
    @PutMapping
    public ResponseEntity<ApiResponseDTO<TaskDTO>> update(@Valid @RequestBody TaskDTO taskDTO) {
        Long userId = securityUtils.getCurrentUserId();
        Task task = taskMapper.taskDTOToTask(taskDTO);
        Task updated = taskService.update(task, userId);
        return ApiResponseFactory.success(taskMapper.taskToTaskDTO(updated), "Task updated successfully");
    }

    @Operation(summary = "Find task by ID",
            description = "Retrieve a task by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasAuthority('TASK_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TaskDTO>> findById(@PathVariable Long id) throws NotFoundException {

        Task task = taskService.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        TaskDTO taskDTO = taskMapper.taskToTaskDTO(task);
        return ApiResponseFactory.success(taskDTO, "Task found");
    }

    @Operation(summary = "List all tasks",
            description = "Returns a list of all tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasAuthority('TASK_READ')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TaskDTO>>> findAll() {
        List<TaskDTO> taskDTO = taskService.findAll()
                .stream().map(taskMapper::taskToTaskDTO).toList();

        return ApiResponseFactory.success(taskDTO, "Tasks found");
    }

    @Operation(summary = "Delete task by ID",
            description = "Deletes a task by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasAuthority('TASK_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(@PathVariable Long id) {
        taskService.delete(id);

        return ApiResponseFactory.success(null, "Task deleted");
    }
}
