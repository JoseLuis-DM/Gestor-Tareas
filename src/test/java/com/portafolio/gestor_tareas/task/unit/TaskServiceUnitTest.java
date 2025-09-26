package com.portafolio.gestor_tareas.task.unit;

import com.portafolio.gestor_tareas.task.application.TaskServiceImpl;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class TaskServiceUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SpringTaskRepository springTaskRepository;

    @Mock
    private SpringUserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task inputTask;
    private UserEntity userEntity;
    private Task updateTask;

    @BeforeEach
    void setUp() {

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstname("Test User");
        userEntity.setEmail("test@example.com");
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

        when(springTaskRepository.findByUserIdAndTitleIgnoreCase(1L, "Test Task"))
                .thenReturn(Optional.of(inputTask));

        assertThrows(IllegalArgumentException.class, () -> taskService.save(inputTask, 1L));

        verify(springTaskRepository, times(1)).findByUserIdAndTitleIgnoreCase(1L, "Test Task");
        verify(taskRepository, never()).save(any());
    }

    // Test that validates that a task is not saved when the user is not found
    @Test
    void shouldNotSaveTaskWhenUserNotFound() {

        when(springTaskRepository.findByUserIdAndTitleIgnoreCase(1L, "Test Task"))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> taskService.save(inputTask, 1L));

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

        Task result = taskService.update(updateTask, 1L);

        assertEquals("Test Updated", result.getTitle());
        assertEquals(userEntity, result.getUser());

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(updateTask);
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

        assertThrows(RuntimeException.class, () -> taskService.update(updated, 1L));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any());
    }

    //  Test that attempts to update a task that does not belong to the logged-in user
    @Test
    void shouldNotUpdateTaskWhenUserDoesNotOwnTask() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));

        assertThrows(RuntimeException.class, () -> taskService.update(inputTask, 99L));

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any());
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

        taskService.delete(1L);

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    // Test to delete a task that does not exist
    @Test
    void shouldThrowExceptionWhenDeletingNonexistentTask() {

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.delete(99L));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).deleteById(any());
    }
}
