package com.portafolio.gestor_tareas.task.infrastructure;

import com.portafolio.gestor_tareas.config.application.JwtService;
import com.portafolio.gestor_tareas.config.infrastructure.SecurityConfig;
import com.portafolio.gestor_tareas.config.infrastructure.SecurityUtils;
import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskService;
import com.portafolio.gestor_tareas.task.infrastructure.dto.BulkTaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final SecurityConfig securityConfig;

    @Operation(summary = "Register a new task",
            description = "Creates a new task in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationError"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/InvalidJson"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/TaskAlreadyExists"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<TaskDTO>> register(@Valid @RequestBody TaskDTO taskDTO) {
        return taskService.save(taskDTO);
    }

    @Operation(summary = "Update an existing task",
            description = "Updates task details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationError"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/InvalidJson"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_WRITE')")
    @PutMapping
    public ResponseEntity<ApiResponseDTO<TaskDTO>> update(
            @Valid @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = securityUtils.getCurrentUserId();
        Task task = taskMapper.taskDTOToTask(taskDTO);
        Task updated = taskService.update(task, userId, userDetails);

        return ApiResponseFactory.success(taskMapper.taskToTaskDTO(updated), "Task updated successfully");
    }

    @Operation(summary = "Find task by ID",
            description = "Retrieve a task by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TaskDTO>> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails)
            throws NotFoundException {

        Task task = taskService.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        securityConfig.checkAccess(task.getUser().getId(), userDetails);
        TaskDTO taskDTO = taskMapper.taskToTaskDTO(task);
        return ApiResponseFactory.success(taskDTO, "Task found");
    }

    @Operation(summary = "List all tasks",
            description = "Returns a list of all tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks found"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_READ')")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TaskDTO>>> findAll(@AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = securityUtils.getCurrentUserId();

        List<TaskDTO> taskDTO = taskService.findAll()
                .stream()
                .filter(task -> {
                    if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                        return true;
                    }
                    return task.getUser() != null && task.getUser().getId().equals(currentUserId);
                })
                .map(taskMapper::taskToTaskDTO)
                .toList();

        return ApiResponseFactory.success(taskDTO, "Tasks found");
    }

    @Operation(summary = "Delete task by ID",
            description = "Deletes a task by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        taskService.delete(id, userDetails);

        return ApiResponseFactory.success(null, "Task deleted");
    }

    @Operation(summary = "Update task status by its ID",
            description = "Update the completion status of a task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/InvalidTaskComplete"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_WRITE')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponseDTO<Void>> updateCompletionStatus(
            @PathVariable Long id,
            @RequestParam boolean completed,
            @AuthenticationPrincipal UserDetails userDetails) {

        taskService.updateCompletionStatus(id, completed, userDetails);
        String msg = completed ? "Task marked as completed" : "Task marked as not completed";
        return ApiResponseFactory.success(null, msg);
    }

    @Operation(summary = "Add tasks to a user",
            description = "Tasks are added to the user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task assigned successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_ASSIGN')")
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> addTasksToUser(
            @PathVariable Long userId,
            @RequestBody List<Long> taskIds
    ) {
        return taskService.addTasksToUser(userId, taskIds);
    }

    @Operation(summary = "Add tasks to users",
            description = "Tasks are added to users in bulk")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All tasks assigned successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_ASSIGN')")
    @PostMapping("/users")
    public ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> addTasksToUsers(
            @RequestBody BulkTaskDTO bulkTaskDTO
    ) {
        return taskService.addTasksToUsers(bulkTaskDTO);
    }

    @Operation(summary = "Task deleted from a user",
            description = "The task is deleted from the user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task removed successfully"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/UserDontHaveTasks"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_UNASSIGN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> unassignTasksFromUser(
            @PathVariable Long userId,
            @RequestBody List<Long> taskIds
    ) {
        return taskService.unassignTasksFromUser(userId, taskIds);
    }

    @Operation(summary = "Unassign tasks from users",
            description = "Removes task assignments in bulk for multiple users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All tasks successfully deleted"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest", content = @Content),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/AccessDenied"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/UserDontHaveTasks"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalError")
    })
    @PreAuthorize("hasAuthority('TASK_UNASSIGN')")
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> unassignTasksFromUsers(
            @RequestBody BulkTaskDTO bulkTaskDTO
    ) {
        return taskService.unassignTasksFromUsers(bulkTaskDTO)  ;
    }
}
