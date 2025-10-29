package com.portafolio.gestor_tareas.task.application;

import com.portafolio.gestor_tareas.config.infrastructure.SecurityConfig;
import com.portafolio.gestor_tareas.config.infrastructure.SecurityUtils;
import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.exception.domain.InvalidTaskCompleteException;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.exception.domain.TaskAlreadyExistException;
import com.portafolio.gestor_tareas.exception.domain.UserDontHaveTasksException;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.domain.TaskService;
import com.portafolio.gestor_tareas.task.infrastructure.dto.BulkTaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskAssignmentDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final SpringTaskRepository springTaskRepository;
    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;
    private final SecurityUtils securityUtils;

    private Task task;

    private static final String USER_NOT_FOUND = "User not found";
    private static final String TASK_NOT_FOUND = "Task not found";
    private static final String ERRORS = "errors";

    @Override
    @Transactional
    public ResponseEntity<ApiResponseDTO<TaskDTO>> save(TaskDTO taskDTO) {

        Task taskInput = taskMapper.taskDTOToTask(taskDTO);

        boolean isOwnTask = taskDTO.ownTask() != null && taskDTO.ownTask();

        User newUser = null;

        if (isOwnTask) {
            Long currentUserId = securityUtils.getCurrentUserId();
            newUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        } else if (taskDTO.userId() != null){
            newUser = userRepository.findById(taskDTO.userId())
                    .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        }

        if (newUser != null) {
            boolean taskExists = taskRepository
                    .findByUserIdAndTitleIgnoreCase(newUser.getId(), taskInput.getTitle())
                    .isPresent();

            if (taskExists) {
                throw new TaskAlreadyExistException("The task is already exists");
            }
        }

        taskInput.setCompleted(false);
        taskInput.setUser(newUser);

        TaskEntity taskEntity = taskMapper.taskToTaskEntity(taskInput);

        TaskEntity savedEntity = springTaskRepository.save(taskEntity);

        Task savedTask = taskMapper.taskEntityToTask(savedEntity);

        TaskDTO response = taskMapper.taskToTaskDTO(savedTask);

        return ApiResponseFactory.created(response, "Task created successfully");
    }

    @Transactional
    public Task update(Task task, Long userId, UserDetails userDetails) {

        Task updateTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND));

        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(USER_NOT_FOUND);
        }

        securityConfig.checkAccess(updateTask.getUser().getId(), userDetails);

        updateTask.setTitle(task.getTitle());
        updateTask.setDescription(task.getDescription());
        updateTask.setCompleted(task.isCompleted());

        return taskRepository.save(updateTask);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public void delete(Long id, UserDetails userDetails) {
         task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The task does not exist"));

        securityConfig.checkAccess(task.getUser().getId(), userDetails);

        taskRepository.deleteById(id);
    }

    @Override
    public void updateCompletionStatus(Long id, boolean complete, UserDetails userDetails) {

        task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The task does not exist"));

        securityConfig.checkAccess(task.getUser().getId(), userDetails);

        if (complete) {
            if (task.isCompleted()) {
                throw new InvalidTaskCompleteException("The task is already completed");
            }
            task.setCompleted(true);
        } else {
            if (!task.isCompleted()) {
                throw new InvalidTaskCompleteException("The task is not complete, it cannot be marked as not completed");
            }
            task.setCompleted(false);
        }

        taskRepository.save(task);
    }

    @Override
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> addTasksToUser(Long userId, List<Long> taskIds) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        if (taskIds.size() == 1) {
            Long taskId = taskIds.get(0);

            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND));

            task.setUser(user);
            taskRepository.save(task);

            return ApiResponseFactory.success(null, "Task assigned successfully");
        }

        List<TaskAssignmentDTO> assignments = List.of(new TaskAssignmentDTO(userId, taskIds));
        Map<String, List<String>> result = processTaskForUser(assignments);

        String message = result.get(ERRORS).isEmpty()
                ? "All tasks assigned successfully"
                : "Some assignments failed";

        return ApiResponseFactory.success(null, message);
    }

    @Override
    public ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> addTasksToUsers(BulkTaskDTO bulkTaskDTO) {

        Map<String, List<String>> result = processTaskForUser(bulkTaskDTO.assignments());

        String message = result.get(ERRORS).isEmpty()
                ? "All tasks assigned successfully"
                : "Some assignments failed";

        return ApiResponseFactory.success(result, message);
    }

    @Override
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> unassignTasksFromUser(Long userId, List<Long> taskIds) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        if (taskIds.size() == 1) {
            Long taskId = taskIds.get(0);

            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND));

            if (!user.equals(task.getUser())) {
                throw new UserDontHaveTasksException(
                        "Task with id " + taskId + " is not assigned to user " + user.getId());
            }

            task.setUser(null);
            taskRepository.save(task);

            return ApiResponseFactory.success(null, "Task removed successfully");
        }

        List<TaskAssignmentDTO> assignments = List.of(new TaskAssignmentDTO(userId, taskIds));
        Map<String, List<String>> result = removeTasksFromUsers(assignments);

        String message = result.get(ERRORS).isEmpty()
                ? "All tasks successfully unassigned"
                : "Some tasks could not be unassigned";

        return ApiResponseFactory.success(null, message);
    }

    @Override
    public ResponseEntity<ApiResponseDTO<Map<String, List<String>>>> unassignTasksFromUsers(BulkTaskDTO bulkTaskDTO) {

        Map<String, List<String>> result = removeTasksFromUsers(bulkTaskDTO.assignments());

        String message = result.get(ERRORS).isEmpty()
                ? "All tasks successfully unassigned"
                : "Some tasks could not be unassigned";

        return ApiResponseFactory.success(result, message);
    }

    private Map<String, List<String>> processTaskForUser(List<TaskAssignmentDTO> assignments) {

        List<String> successMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (TaskAssignmentDTO assignment : assignments) {
            try {
                User user = userRepository.findById(assignment.userId())
                        .orElseThrow(() -> new NotFoundException("User with id " + assignment.userId() + " not found"));

                for (Long taskId : assignment.taskIds()) {

                    task = taskRepository.findById(taskId)
                            .orElseThrow(() -> new NotFoundException("Task with id " + taskId + " not found"));

                    task.setUser(user);
                    taskRepository.save(task);
                    successMessages.add("Task " + taskId + " assigned to user " + user.getId());
                }

            } catch (NotFoundException e) {
                errorMessages.add(e.getMessage());
            }
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("success", successMessages);
        result.put(ERRORS, errorMessages);
        return result;
    }

    private Map<String, List<String>> removeTasksFromUsers(List<TaskAssignmentDTO> deletions) {

        List<String> successMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (TaskAssignmentDTO deletion : deletions) {
            try {
                User user = userRepository.findById(deletion.userId())
                        .orElseThrow(() -> new NotFoundException("User with id " + deletion.userId() + " not found"));

                List<Task> userTasks = taskRepository.findByUser(user);

                if (userTasks.isEmpty()) {
                    throw new UserDontHaveTasksException("User with id " + deletion.userId() + " doesn’t have any tasks");
                } else {
                    for (Long taskId : deletion.taskIds()) {

                        task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new NotFoundException("Task with id " + taskId + " not found"));

                        if (!user.equals(task.getUser())) {
                            throw new UserDontHaveTasksException(
                                    "Task with id " + taskId + " is not assigned to user " + user.getId());
                        }

                        task.setUser(null);
                        taskRepository.save(task);
                        successMessages.add("Task " + taskId + " removed from user " + user.getId());
                    }
                }
            } catch (NotFoundException | UserDontHaveTasksException e) {
                errorMessages.add(e.getMessage());
            }
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("success", successMessages);
        result.put(ERRORS, errorMessages);
        return result;
    }
}
