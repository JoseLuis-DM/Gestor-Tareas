package com.portafolio.gestor_tareas.task.unit;

import com.portafolio.gestor_tareas.config.infrastructure.SecurityConfig;
import com.portafolio.gestor_tareas.exception.domain.ForbiddenException;
import com.portafolio.gestor_tareas.exception.domain.InvalidTaskCompleteException;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.exception.domain.TaskAlreadyExistException;
import com.portafolio.gestor_tareas.task.application.TaskServiceImpl;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TaskServiceUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SpringTaskRepository springTaskRepository;

    @Mock
    private SpringUserRepository userRepository;

    @Mock
    private SecurityConfig securityConfig;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task inputTask;
    private UserEntity userEntity;
    private Task updateTask;
    private UserEntity user;

    @BeforeEach
    void setUp() {

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstname("Test User");
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("123456");

        user = new UserEntity();
        userEntity.setId(2L);
        userEntity.setFirstname("User");
        userEntity.setEmail("testUser@example.com");
        userEntity.setPassword("123456");

        inputTask = new Task();
        inputTask.setId(1L);
        inputTask.setTitle("Test Task");
        inputTask.setDescription("Sample test");
        inputTask.setCompleted(false);
        inputTask.setUser(userEntity);

        updateTask = new Task();
        updateTask.setId(1L);
        updateTask.setTitle("Test Updated");
        updateTask.setDescription("Sample test updated");
        updateTask.setCompleted(false);
        updateTask.setUser(userEntity);
    }

    /*
        CREATE TASK
    */

    // Test that validates the creation of a task
    @Test
    void shouldSaveTaskSuccessfully() {

        when(springTaskRepository.findByUserIdAndTitleIgnoreCase(1L, "Test Task"))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(taskRepository.save(any(Task.class))).thenReturn(inputTask);

        Task result = taskService.save(inputTask, 1L);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals(userEntity, result.getUser());

        verify(springTaskRepository, times(1))
                .findByUserIdAndTitleIgnoreCase(1L, "Test Task");
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(inputTask);
    }

    // Test that validates that a task is not saved when the task already exists
    @Test
    void shouldNotSaveTaskWhenTaskAlreadyExists() {

        TaskEntity inputEntity = new TaskEntity();
        inputEntity.setId(1L);
        inputEntity.setTitle("Test Task");
        inputEntity.setDescription("Sample test");
        inputEntity.setCompleted(false);
        inputEntity.setUser(userEntity);

        when(springTaskRepository.findByUserIdAndTitleIgnoreCase(1L, "Test Task"))
                .thenReturn(Optional.of(inputEntity));

        assertThrows(TaskAlreadyExistException.class, () -> taskService.save(inputTask, 1L));

        verify(springTaskRepository, times(1)).findByUserIdAndTitleIgnoreCase(1L, "Test Task");
        verify(taskRepository, never()).save(any());
    }

    // Test that validates that a task is not saved when the user is not found
    @Test
    void shouldNotSaveTaskWhenUserNotFound() {

        when(springTaskRepository.findByUserIdAndTitleIgnoreCase(1L, "Test Task"))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.save(inputTask, 1L));

        verify(springTaskRepository, times(1))
                .findByUserIdAndTitleIgnoreCase(1L, "Test Task");
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    /*
        UPDATE TASK
    */

    // Test that validates a task update
    @Test
    void shouldUpdateTaskSuccessfully() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updateTask);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = mock(UserDetails.class);

        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));

        Task result = taskService.update(updateTask, 1L, userDetails);

        assertEquals("Test Updated", result.getTitle());
        assertEquals(userEntity, result.getUser());

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(updateTask);
        verify(securityConfig, times(1)).checkAccess(anyLong(), any(UserDetails.class));
    }

    // Test that attempts to update a task that does not exist
    @Test
    void shouldNotUpdateTaskWhenTaskNotFound() {

        Task updated = new Task(
                99L,
                "Test Updated",
                "Simple test updated",
                false,
                userEntity
        );

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        UserDetails userDetails = mock(UserDetails.class);

        assertThrows(NotFoundException.class, () -> taskService.update(updated, 1L, userDetails));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any());
    }

    // Test that attempts to update a task that does not belong to the logged-in user
    @Test
    void shouldNotUpdateTaskWhenUserDoesNotOwnTask() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserDetails userDetails = mock(UserDetails.class);

        doThrow(new ForbiddenException("Forbidden"))
                .when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));

        assertThrows(ForbiddenException.class, () -> taskService.update(inputTask, 2L, userDetails));

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any());
        verify(securityConfig, times(1)).checkAccess(anyLong(), any(UserDetails.class));
    }

    /*
        FIND TASKS
    */

    // Test that validates that a task was found
    @Test
    void shouldFindTaskByIdSuccessfully() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));

        Optional<Task> foundTask = taskService.findById(1L);

        assertTrue(foundTask.isPresent());
        assertEquals("Test Task", foundTask.get().getTitle());

        verify(taskRepository, times(1)).findById(1L);
    }

    // Test that validates that a task was not found because it does not exist
    @Test
    void shouldReturnEmptyWhenTaskNotFound() {

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Task> foundTask = taskService.findById(99L);

        assertFalse(foundTask.isPresent());

        verify(taskRepository, times(1)).findById(99L);
    }

    /*
        DELETE TASK
    */

    // Successful task deletion test
    @Test
    void shouldDeleteTaskSuccessfully() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));
        doNothing().when(taskRepository).deleteById(1L);

        UserDetails userDetails = mock(UserDetails.class);

        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));

        taskService.delete(1L, userDetails);

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    // Test to delete a task that does not exist
    @Test
    void shouldThrowExceptionWhenDeletingNonexistentTask() {

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        UserDetails userDetails = mock(UserDetails.class);

        assertThrows(RuntimeException.class, () -> taskService.delete(99L, userDetails));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).deleteById(any());
    }

    /*
        updateCompletionStatus (PATCH)
    */

    // Field update completed successfully
    @Test
    void shouldUpdatedCompleteTaskSuccessfully() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));

        UserDetails userDetails = mock(UserDetails.class);

        taskService.updateCompletionStatus(1L, true, userDetails);

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(inputTask);
    }

    // Test that attempts to update the completion status of a non-existent task
    @Test
    void shouldThrowExceptionWhenTaskNoExist() {

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        UserDetails userDetails = mock(UserDetails.class);

        assertThrows(NotFoundException.class, () -> taskService.updateCompletionStatus(99L, true, userDetails));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any());
    }

    // Test where an attempt is made to update a completed task to completed
    @Test
    void shouldThrowExceptionWhenTaskIsNotComplete() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));

        UserDetails userDetails = mock(UserDetails.class);

        assertThrows(InvalidTaskCompleteException.class, () -> taskService.updateCompletionStatus(1L, false, userDetails));

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any());
    }
}
