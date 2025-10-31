package com.portafolio.gestor_tareas.task.unit;

import com.portafolio.gestor_tareas.config.TestTaskFactory;
import com.portafolio.gestor_tareas.config.infrastructure.SecurityConfig;
import com.portafolio.gestor_tareas.exception.domain.*;
import com.portafolio.gestor_tareas.task.application.TaskServiceImpl;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.infrastructure.dto.BulkTaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskAssignmentDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.mapper.TaskMapper;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
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
    private UserRepository userRepository;

    @Mock
    private SecurityConfig securityConfig;

    @Mock
    private TaskMapper taskMapper;

    //@Mock
    private TestTaskFactory testTaskFactory;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UserEntity userEntity;
    private User userDomain;
    private User userDomainTwo;
    private Task inputTask;
    private Task taskTwo;
    private Task taskThree;
    private Task taskFour;
    private Task taskFive;
    private TaskDTO taskToUpdate;
    private BulkTaskDTO bulkTaskDTO;

    @BeforeEach
    void setUp() {

        userDomain = new User();
        userDomain.setId(1L);
        userDomain.setFirstname("Test User");
        userDomain.setEmail("test@example.com");
        userDomain.setPassword("123456");

        userEntity = new UserEntity();
        userEntity.setId(2L);
        userEntity.setFirstname("User");
        userEntity.setEmail("testUser@example.com");
        userEntity.setPassword("123456");

        userDomainTwo = new User();
        userDomainTwo.setId(3L);
        userDomainTwo.setFirstname("User Test ");
        userDomainTwo.setEmail("testuser@test.com");
        userDomainTwo.setPassword("654321");

        inputTask = new Task();
        inputTask.setId(1L);
        inputTask.setTitle("Test Task");
        inputTask.setDescription("Sample test");
        inputTask.setCompleted(false);
        inputTask.setUser(userDomain);

        taskTwo = new Task();
        taskTwo.setId(2L);
        taskTwo.setTitle("Test Two");
        taskTwo.setDescription("Sample test two");
        taskTwo.setCompleted(false);

        taskThree = new Task();
        taskThree.setId(3L);
        taskThree.setTitle("Test Three");
        taskThree.setDescription("Sample test three");
        taskThree.setCompleted(false);

        taskFour = new Task();
        taskFour.setId(4L);
        taskFour.setTitle("Test Four");
        taskFour.setDescription("Sample test four");
        taskFour.setCompleted(false);

        taskFive = new Task();
        taskFive.setTitle("Test Five");
        taskFive.setId(5L);
        taskFive.setDescription("Sample test five");
        taskFive.setCompleted(false);

        testTaskFactory = new TestTaskFactory();
    }

    private TaskDTO createTaskDTOWithId(Long id, String title, String description, Long userId) {

        return testTaskFactory.createTaskDTOWithId(id, title, description, userId);
    }

    public TaskDTO createUnassignedTaskDTO(String title, String description) {
        return testTaskFactory.createUnassignedfTaskDTO(title ,description);
    }

    public TaskDTO createOwnTask(String title, String description) {
        return testTaskFactory.createOwnTask(title, description);
    }

    public TaskDTO createAssingedTaskDTO(String title, String description, Long userID) {
        return testTaskFactory.createAssingedTaskDTO(title, description, userID);
    }

    /*
        CREATE TASK
    */

    // Test that validates the creation of a task without being assigned to a user
    @Test
    void shouldSaveTaskSuccessfullyWithoutUser() {

        TaskDTO taskDTO = createUnassignedTaskDTO("Test Task", "Sample Test");

        Task taskDomain = new Task(
                null,
                taskDTO.title(),
                taskDTO.description(),
                taskDTO.completed(),
                null
        );

        TaskEntity savedEntity = new TaskEntity();
        savedEntity.setId(1L);
        savedEntity.setTitle(taskDomain.getTitle());
        savedEntity.setDescription(taskDomain.getDescription());
        savedEntity.setCompleted(taskDomain.isCompleted());
        savedEntity.setUser(null);

        TaskDTO responseDTO = createTaskDTOWithId(1L, taskDomain.getTitle(), taskDomain.getDescription(), null);

        when(taskMapper.taskDTOToTask(taskDTO)).thenReturn(taskDomain);
        when(taskMapper.taskToTaskEntity(taskDomain)).thenReturn(savedEntity);
        when(springTaskRepository.save(any(TaskEntity.class))).thenReturn(savedEntity);
        when(taskMapper.taskEntityToTaskDTO(savedEntity)).thenReturn(responseDTO);

        TaskDTO response = taskService.save(taskDTO);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("Test Task", response.title()),
                () -> assertEquals("Sample Test", response.description()),
                () -> assertNull(response.userId())
        );

        verify(taskMapper, times(1)).taskDTOToTask(taskDTO);
        verify(taskMapper, times(1)).taskToTaskEntity(taskDomain);
        verify(springTaskRepository, times(1)).save(savedEntity);
        verify(taskMapper, times(1)).taskEntityToTaskDTO(savedEntity);
    }

    // Test that validates the creation of a task and its assignment to a user
    @Test
    void shouldSaveTaskSuccessfullyWithUser() {

        TaskDTO taskDTO = createAssingedTaskDTO("Test Task", "Sample Test", userEntity.getId());

        User newUser = new User();
        newUser.setId(userEntity.getId());
        newUser.setFirstname(userEntity.getFirstname());
        newUser.setEmail(userEntity.getEmail());
        newUser.setPassword(userEntity.getPassword());

        Task taskDomain = new Task(
                null,
                taskDTO.title(),
                taskDTO.description(),
                taskDTO.completed(),
                newUser
        );

        TaskEntity savedEntity = new TaskEntity();
        savedEntity.setId(1L);
        savedEntity.setTitle(taskDomain.getTitle());
        savedEntity.setDescription(taskDomain.getDescription());
        savedEntity.setCompleted(taskDomain.isCompleted());
        savedEntity.setUser(userEntity);

        TaskDTO responseDTO = createTaskDTOWithId(
                1L,
                taskDomain.getTitle(),
                taskDomain.getDescription(),
                newUser.getId()
        );

        when(taskMapper.taskDTOToTask(taskDTO)).thenReturn(taskDomain);
        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(taskMapper.taskToTaskEntity(taskDomain)).thenReturn(savedEntity);
        when(springTaskRepository.save(any(TaskEntity.class))).thenReturn(savedEntity);
        when(taskMapper.taskEntityToTaskDTO(savedEntity)).thenReturn(responseDTO);

        TaskDTO response = taskService.save(taskDTO);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1L, response.id()),
                () -> assertEquals("Test Task", response.title()),
                () -> assertEquals("Sample Test", response.description()),
                () -> assertNotNull(response.userId())
        );

        verify(taskMapper, times(1)).taskDTOToTask(taskDTO);
        verify(userRepository, times(1)).findById(newUser.getId());
        verify(taskMapper, times(1)).taskToTaskEntity(taskDomain);
        verify(springTaskRepository, times(1)).save(savedEntity);
        verify(taskMapper, times(1)).taskEntityToTaskDTO(savedEntity);
    }

    // Test that validates that a task is not saved when the task already exists
    @Test
    void shouldNotSaveTaskWhenTaskAlreadyExists() {

        TaskDTO taskDTO = createAssingedTaskDTO("Test Task", "Sample Test", userDomain.getId());

        Task taskDomain = new Task(
                null,
                taskDTO.title(),
                taskDTO.description(),
                taskDTO.completed(),
                userDomain
        );

        when(taskMapper.taskDTOToTask(taskDTO)).thenReturn(taskDomain);
        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findByUserIdAndTitleIgnoreCase(userDomain.getId(), "Test Task"))
                .thenReturn(Optional.of(inputTask));

        assertThrows(TaskAlreadyExistException.class, () -> taskService.save(taskDTO));

        verify(taskRepository, times(1)).findByUserIdAndTitleIgnoreCase(userDomain.getId(), "Test Task");
        verify(taskRepository, never()).save(any());
    }

    /*
        UPDATE TASK
    */

    // Test that validates a task update
    @Test
    void shouldUpdateTaskSuccessfully() {

        taskToUpdate = createTaskDTOWithId(
                1L,
                "Test Updated",
                "Sample test updated",
                userDomain.getId()
        );

        UserEntity user = new UserEntity();
        user.setId(userDomain.getId());
        user.setFirstname(userDomain.getFirstname());
        user.setEmail(userDomain.getEmail());
        user.setPassword(userDomain.getPassword());

        TaskEntity savedEntity = new TaskEntity();
        savedEntity.setId(taskToUpdate.id());
        savedEntity.setTitle(taskToUpdate.title());
        savedEntity.setDescription(taskToUpdate.description());
        savedEntity.setCompleted(taskToUpdate.completed());
        savedEntity.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userDomain));
        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));
        when(taskMapper.taskToTaskEntity(any(Task.class))).thenReturn(savedEntity);
        when(springTaskRepository.save(any(TaskEntity.class))).thenReturn(savedEntity);
        when(taskMapper.taskEntityToTaskDTO(any(TaskEntity.class))).thenReturn(taskToUpdate);

        UserDetails userDetails = mock(UserDetails.class);

        TaskDTO response = taskService.update(taskToUpdate, 1L, userDetails);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("Test Updated", response.title()),
                () -> assertEquals("Sample test updated", response.description()),
                () -> assertEquals(userDomain.getId(), response.userId())
        );

        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(userDomain.getId());
        verify(securityConfig, times(1)).checkAccess(anyLong(), any(UserDetails.class));
        verify(springTaskRepository, times(1)).save(savedEntity);
        verify(taskMapper, times(1)).taskEntityToTaskDTO(savedEntity);
    }

    // Test that attempts to update a task that does not exist
    @Test
    void shouldNotUpdateTaskWhenTaskNotFound() {

        taskToUpdate = createTaskDTOWithId(
                99L,
                "Test Updated",
                "Simple test updated",
                userDomain.getId()
        );

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        UserDetails userDetails = mock(UserDetails.class);

        assertThrows(NotFoundException.class, () -> taskService.update(taskToUpdate, 1L, userDetails));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any());
    }

    // Test that attempts to update a task that does not belong to the logged-in user
    @Test
    void shouldNotUpdateTaskWhenUserDoesNotOwnTask() {

        taskToUpdate = createTaskDTOWithId(
                inputTask.getId(),
                inputTask.getTitle(),
                inputTask.getDescription(),
                inputTask.getUser().getId()
        );

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userDomain));

        UserDetails userDetails = mock(UserDetails.class);

        doThrow(new ForbiddenException("Forbidden"))
                .when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));

        assertThrows(ForbiddenException.class, () -> taskService.update(taskToUpdate, 2L, userDetails));

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

        TaskDTO task = createTaskDTOWithId(
                inputTask.getId(),
                inputTask.getTitle(),
                inputTask.getDescription(),
                userDomain.getId()
        );

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));
        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));
        when(taskMapper.taskToTaskDTO(any(Task.class))).thenReturn(task);

        UserDetails userDetails = mock(UserDetails.class);

        TaskDTO foundTask = taskService.findById(1L, userDetails);

        assertAll(
                () -> assertNotNull(foundTask),
                () -> assertEquals(inputTask.getTitle(), foundTask.title()),
                () -> assertEquals(inputTask.getDescription(), foundTask.description())
        );

        verify(taskRepository, times(1)).findById(1L);
    }

    // Test that validates that a task was not found because it does not exist
    @Test
    void shouldReturnEmptyWhenTaskNotFound() {

        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        UserDetails userDetails = mock(UserDetails.class);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> taskService.findById(99L, userDetails)
        );

        assertEquals("Not found exception. Task not found", exception.getMessage());

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
        updateCompletionStatus
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

        assertThrows(
                NotFoundException.class,
                () -> taskService.updateCompletionStatus(99L, true, userDetails));

        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any());
    }

    // Test where an attempt is made to update a completed task to completed
    @Test
    void shouldThrowExceptionWhenTaskIsNotComplete() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inputTask));

        UserDetails userDetails = mock(UserDetails.class);

        assertThrows(
                InvalidTaskCompleteException.class,
                () -> taskService.updateCompletionStatus(1L, false, userDetails));

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any());
    }

    /*
        addTaskToUser
    */

    // Adding a task successfully
    @Test
    void shouldAddTaskToUserSuccessfully() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(taskTwo.getId())).thenReturn(Optional.of(taskTwo));
        when(taskRepository.save(taskTwo)).thenReturn(taskTwo);

        List<Long> taskId = List.of(taskTwo.getId());

        Map<String, List<String>> response = taskService.addTasksToUser(userDomain.getId(), taskId);

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("errors").isEmpty()),
                () -> assertEquals(userDomain, taskTwo.getUser())
        );

        verify(userRepository, times(1)).findById(userDomain.getId());
        verify(taskRepository, times(1)).findById(taskTwo.getId());
        verify(taskRepository, times(1)).save(taskTwo);
    }

    // Adding multiple tasks correctly
    @Test
    void shouldAddMultipleTasksToUserSuccessfully() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Long> taskIds = List.of(2L, 3L, 4L);

        Map<String, List<String>> response = taskService.addTasksToUser(userDomain.getId(), taskIds);

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("errors").isEmpty()),
                () -> assertEquals(userDomain, taskTwo.getUser()),
                () -> assertNotNull(taskThree.getUser())
        );

        verify(userRepository, times(2)).findById(userDomain.getId());
        verify(taskRepository, times(3)).findById(anyLong());
        verify(taskRepository, times(3)).save(any(Task.class));
    }

    // Some tasks are added and others are not because they do not exist
    @Test
    void shouldSaveExistingTasks() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.save(taskThree)).thenReturn(taskThree);

        List<Long> taskIds = List.of(3L, 11L, 10L);

        Map<String, List<String>> response = taskService.addTasksToUser(userDomain.getId(), taskIds);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1, response.get("errors").size()),
                () -> assertFalse(response.get("success").isEmpty()),
                () -> assertEquals(userDomain.getId(), taskThree.getUser().getId())
        );

        verify(userRepository, times(2)).findById(userDomain.getId());
        verify(taskRepository, times(1)).findById(3L);
        verify(taskRepository, times(1)).save(taskThree);
    }

    // Problem due to non-existent user
    @Test
    void shouldReturnNotFoundBecauseUserDoesNotExist() {

        List<Long> taskIds = List.of(2L, 3L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> taskService.addTasksToUser(99L, taskIds));

        assertNotNull(exception);
        assertEquals("Not found exception. User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(99L);
        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }

    // Error when the only task to add does not exist
    @Test
    void shouldReturnNotFoundBecauseTaskDoesNotExist() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        List<Long> taskIds = List.of(99L);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> taskService.addTasksToUser(userDomain.getId(), taskIds));

        assertNotNull(exception);
        assertEquals("Not found exception. Task not found", exception.getMessage());

        verify(userRepository, times(1)).findById(userDomain.getId());
        verify(taskRepository, times(1)).findById(99L);
        verify(taskRepository, never()).save(any());
    }

    /*
        addTaskToUsers
    */

    // Aggregation of multiple tasks to multiple users
    @Test
    void shouldAddMultipleTaskTMultipleUserSuccessfully() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomain.getId(), List.of(2L, 3L)),
                        new TaskAssignmentDTO(userDomainTwo.getId(), List.of(4L, 5L))
                )
        );

        Map<String, List<String>> response = taskService.addTasksToUsers(bulkTaskDTO);

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("errors").isEmpty()),
                () -> assertTrue(response.get("success").contains("Task 2 assigned to user 1"))
        );

        verify(userRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(4)).findById(anyLong());
        verify(taskRepository, times(4)).save(any(Task.class));
    }

    // Adding tasks to existing users
    @Test
    void shouldAddTaskToExistingUsers() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomain.getId(), List.of(2L, 3L)),
                        new TaskAssignmentDTO(99L, List.of(4L, 5L))
                )
        );

        Map<String, List<String>> response = taskService.addTasksToUsers(bulkTaskDTO);

        List<String> errors = response.get("errors");
        List<String> success = response.get("success");

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(success.contains("Task 2 assigned to user 1")),
                () -> assertEquals(1, errors.size()),
                () -> assertTrue(errors.get(0).contains("User with id 99 not found")),
                () -> assertEquals(2, success.size()),
                () -> assertTrue(success.contains("Task 2 assigned to user " + userDomain.getId())),
                () -> assertTrue(success.contains("Task 3 assigned to user " + userDomain.getId()))
        );

        verify(userRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    // Adding tasks existing to users
    @Test
    void shouldAddTaskExistingToUsers() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));
        when(taskRepository.findById(98L)).thenReturn(Optional.empty());
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomain.getId(), List.of(2L, 98L)),
                        new TaskAssignmentDTO(userDomainTwo.getId(), List.of(5L, 99L))
                )
        );

        Map<String, List<String>> response = taskService.addTasksToUsers(bulkTaskDTO);

        List<String> errors = response.get("errors");
        List<String> success = response.get("success");

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(success.contains("Task 2 assigned to user 1")),
                () -> assertEquals(2, errors.size()),
                () -> assertTrue(errors.get(0).contains("Task with id 98 not found")),
                () -> assertEquals(2, success.size()),
                () -> assertTrue(success.contains("Task 2 assigned to user " + userDomain.getId())),
                () -> assertTrue(success.contains("Task 5 assigned to user " + userDomainTwo.getId()))
        );

        verify(userRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(4)).findById(anyLong());
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    // Adding existing tasks to existing users
    @Test
    void shouldAddExistingTaskToExistingUsers() {

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));
        when(taskRepository.findById(97L)).thenReturn(Optional.empty());
        when(taskRepository.findById(98L)).thenReturn(Optional.empty());
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomain.getId(), List.of(5L, 98L)),
                        new TaskAssignmentDTO(userDomainTwo.getId(), List.of(4L, 97L)),
                        new TaskAssignmentDTO(99L, List.of(3L, 99L))
                )
        );

        Map<String, List<String>> response = taskService.addTasksToUsers(bulkTaskDTO);

        List<String> errors = response.get("errors");
        List<String> success = response.get("success");

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(success.contains("Task 5 assigned to user 1")),
                () -> assertEquals(3, errors.size()),
                () -> assertTrue(errors.stream().anyMatch(e -> e.contains("Task with id 98 not found"))),
                () -> assertTrue(errors.stream().anyMatch(e -> e.contains("Task with id 97 not found"))),
                () -> assertTrue(errors.stream().anyMatch(e -> e.contains("User with id 99 not found"))),
                () -> assertEquals(2, success.size()),
                () -> assertTrue(success.contains("Task 5 assigned to user " + userDomain.getId())),
                () -> assertTrue(success.contains("Task 4 assigned to user " + userDomainTwo.getId()))
        );

        verify(userRepository, times(3)).findById(anyLong());
        verify(taskRepository, times(4)).findById(anyLong());
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    /*
        unassignTasksFromUser
    */

    // Unassign a task successfully
    @Test
    void shouldUnassignTaskToUserSuccessfully() {

        taskThree.setUser(userDomain);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.save(taskThree)).thenReturn(taskThree);

        List<Long> taskId = List.of(3L);

        Map<String, List<String>> response = taskService.unassignTasksFromUser(userDomain.getId(), taskId);

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("errors").isEmpty()),
                () -> assertTrue(response.get("success").contains("Task removed successfully")),
                () -> assertNull(taskThree.getUser())
        );

        verify(userRepository, times(1)).findById(userDomain.getId());
        verify(taskRepository, times(1)).findById(3L);
        verify(taskRepository, times(1)).save(taskThree);
    }

    // Unassign multiple tasks correctly
    @Test
    void shouldUnassignMultipleTaskToUserSuccessfully() {

        taskThree.setUser(userDomain);
        taskFour.setUser(userDomain);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findByUser(userDomain)).thenReturn(List.of(taskThree, taskFour));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Long> taskIds = List.of(3L, 4L);

        Map<String, List<String>> response = taskService.unassignTasksFromUser(userDomain.getId(), taskIds);

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("errors").isEmpty()),
                () -> assertTrue(response.get("success").contains("Task 3 removed from user 1")),
                () -> assertNull(taskThree.getUser()),
                () -> assertNull(taskFour.getUser())
        );

        verify(userRepository, times(2)).findById(userDomain.getId());
        verify(taskRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(1)).findByUser(userDomain);
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    // Some tasks are unassign and others are not because they do not exist
    @Test
    void shouldUnassignExistingTasks() {

        taskFour.setUser(userDomain);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findByUser(userDomain)).thenReturn(List.of(taskFour));
        when(taskRepository.save(taskFour)).thenReturn(taskFour);

        List<Long> taskIds = List.of(4L, 99L);

        Map<String, List<String>> response = taskService.unassignTasksFromUser(userDomain.getId(), taskIds);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1, response.get("errors").size()),
                () -> assertTrue(response.get("success").contains("Task 4 removed from user 1")),
                () -> assertNull(taskFour.getUser())
        );

        verify(userRepository, times(2)).findById(userDomain.getId());
        verify(taskRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(1)).findByUser(userDomain);
        verify(taskRepository, times(1)).save(taskFour);
    }

    // Some tasks are unassigned and others are not because they are not assigned to the user
    @Test
    void shouldUnassignTasksAssignedToUser() {

        taskFour.setUser(userDomain);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));
        when(taskRepository.findByUser(userDomain)).thenReturn(List.of(taskFour));
        when(taskRepository.save(taskFour)).thenReturn(taskFour);

        List<Long> taskIds = List.of(4L, 5L);

        Map<String, List<String>> response = taskService.unassignTasksFromUser(userDomain.getId(), taskIds);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1, response.get("errors").size()),
                () -> assertTrue(response.get("success").contains("Task 4 removed from user 1")),
                () -> assertNull(taskFour.getUser())
        );

        verify(userRepository, times(2)).findById(userDomain.getId());
        verify(taskRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(1)).findByUser(userDomain);
        verify(taskRepository, times(1)).save(taskFour);
    }

    // Problem due to non-existent user
    @Test
    void shouldReturnNotFoundBecauseUserNotExist() {

        List<Long> taskIds = List.of(4L, 5L);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> taskService.unassignTasksFromUser(99L, taskIds));

        assertNotNull(exception);
        assertEquals("Not found exception. User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(99L);
        verify(taskRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any());
    }

    // Error when the task to be unassigned is not assigned to the user
    @Test
    void shouldReturnConflictBecauseTaskIsNotAssigned() {

        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));

        List<Long> taskId = List.of(5L);

        UserDontHaveTasksException exception = assertThrows(
                UserDontHaveTasksException.class,
                () -> taskService.unassignTasksFromUser(userDomainTwo.getId(), taskId));

        assertNotNull(exception);
        assertEquals(
                "User does not have any tasks. Task with id 5 is not assigned to user " + userDomainTwo.getId(),
                exception.getMessage());

        verify(userRepository, times(1)).findById(userDomainTwo.getId());
        verify(taskRepository, times(1)).findById(5L);
        verify(taskRepository, never()).save(any());
    }

    // Error when some tasks to be unassigned are not assigned to the user
    @Test
    void shouldReturnConflictBecauseSomeTaskIsNotAssigned() {

        taskTwo.setUser(userDomain);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));
        when(taskRepository.findByUser(userDomain)).thenReturn(List.of(taskTwo));
        when(taskRepository.save(taskTwo)).thenReturn(taskTwo);

        List<Long> taskIds = List.of(2L, 5L);

        Map<String, List<String>> response = taskService.unassignTasksFromUser(userDomain.getId(), taskIds);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(1, response.get("errors").size()),
                () -> assertTrue(response.get("success").contains("Task 2 removed from user 1")),
                () -> assertNull(taskTwo.getUser())
        );

        verify(userRepository, times(2)).findById(userDomain.getId());
        verify(taskRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(1)).findByUser(userDomain);
        verify(taskRepository, times(1)).save(taskTwo);
    }

    /*
        unassignTasksFromUsers
    */

    // Unassign of multiple tasks to multiple users
    @Test
    void shouldUnassignMultipleTasksFromMultipleUsersSuccessfully() {

        taskTwo.setUser(userDomainTwo);
        taskThree.setUser(userDomain);
        taskFour.setUser(userDomainTwo);
        taskFive.setUser(userDomain);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));
        when(taskRepository.findByUser(userDomain)).thenReturn(List.of(taskThree, taskFive));
        when(taskRepository.findByUser(userDomainTwo)).thenReturn(List.of(taskTwo, taskFour));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomain.getId(), List.of(3L, 5L)),
                        new TaskAssignmentDTO(userDomainTwo.getId(), List.of(2L, 4L))
                )
        );

        Map<String, List<String>> response = taskService.unassignTasksFromUsers(bulkTaskDTO);

        List<String> errors = response.get("errors");
        List<String> success = response.get("success");

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("success").contains("Task 3 removed from user 1")),
                () -> assertEquals(4, success.size()),
                () -> assertTrue(errors.isEmpty()),
                () -> assertNull(taskTwo.getUser()),
                () -> assertNull(taskThree.getUser()),
                () -> assertNull(taskFour.getUser()),
                () -> assertNull(taskFive.getUser())
        );

        verify(userRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(4)).findById(anyLong());
        verify(taskRepository, times(2)).findByUser(any(User.class));
        verify(taskRepository, times(4)).save(any(Task.class));
    }

    // Unassign tasks to existing users
    @Test
    void shouldUnassignTaskToExistingUsers() {

        taskFour.setUser(userDomainTwo);
        taskFive.setUser(userDomainTwo);

        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findById(5L)).thenReturn(Optional.of(taskFive));
        when(taskRepository.findByUser(userDomainTwo)).thenReturn(List.of(taskTwo, taskFour));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomainTwo.getId(), List.of(4L, 5L)),
                        new TaskAssignmentDTO(99L, List.of(2L, 3L))
                )
        );

        Map<String, List<String>> response = taskService.unassignTasksFromUsers(bulkTaskDTO);

        List<String> errors = response.get("errors");
        List<String> success = response.get("success");

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("success").contains("Task 4 removed from user 3")),
                () -> assertEquals(2, success.size()),
                () -> assertTrue(success.contains("Task 4 removed from user 3")),
                () -> assertEquals("Not found exception. User with id 99 not found", errors.get(0)),
                () -> assertNull(taskFour.getUser()),
                () -> assertNull(taskFive.getUser())
        );

        verify(userRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(1)).findByUser(userDomainTwo);
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    // Unassign tasks existing to users
    @Test
    void shouldUnassignTaskExistingToUsers() {

        taskTwo.setUser(userDomainTwo);
        taskThree.setUser(userDomain);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.findById(98L)).thenReturn(Optional.empty());
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        when(taskRepository.findByUser(userDomain)).thenReturn(List.of(taskThree));
        when(taskRepository.findByUser(userDomainTwo)).thenReturn(List.of(taskTwo));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomain.getId(), List.of(3L, 98L)),
                        new TaskAssignmentDTO(userDomainTwo.getId(), List.of(2L, 99L))
                )
        );

        Map<String, List<String>> response = taskService.unassignTasksFromUsers(bulkTaskDTO);

        List<String> errors = response.get("errors");
        List<String> success = response.get("success");

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("success").contains("Task 3 removed from user 1")),
                () -> assertEquals(2, success.size()),
                () -> assertTrue(success.contains("Task 2 removed from user " +  userDomainTwo.getId())),
                () -> assertEquals("Not found exception. Task with id 98 not found", errors.get(0)),
                () -> assertNull(taskTwo.getUser()),
                () -> assertNull(taskThree.getUser())
        );

        verify(userRepository, times(2)).findById(anyLong());
        verify(taskRepository, times(4)).findById(anyLong());
        verify(taskRepository, times(2)).findByUser(any(User.class));
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    // Unassign existing tasks to existing users
    @Test
    void shouldUnassignExistingTaskToExistingUsers() {

        taskTwo.setUser(userDomainTwo);
        taskThree.setUser(userDomain);
        taskFour.setUser(userDomainTwo);

        when(userRepository.findById(userDomain.getId())).thenReturn(Optional.of(userDomain));
        when(userRepository.findById(userDomainTwo.getId())).thenReturn(Optional.of(userDomainTwo));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(taskRepository.findById(2L)).thenReturn(Optional.of(taskTwo));
        when(taskRepository.findById(3L)).thenReturn(Optional.of(taskThree));
        when(taskRepository.findById(4L)).thenReturn(Optional.of(taskFour));
        when(taskRepository.findById(98L)).thenReturn(Optional.empty());
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        when(taskRepository.findByUser(userDomain)).thenReturn(List.of(taskThree));
        when(taskRepository.findByUser(userDomainTwo)).thenReturn(List.of(taskTwo, taskFour));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(userDomain.getId(), List.of(3L, 98L)),
                        new TaskAssignmentDTO(userDomainTwo.getId(), List.of(2L, 4L, 99L)),
                        new TaskAssignmentDTO(99L, List.of(97L))
                )
        );

        Map<String, List<String>> response = taskService.unassignTasksFromUsers(bulkTaskDTO);

        List<String> errors = response.get("errors");
        List<String> success = response.get("success");

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.get("success").contains("Task 3 removed from user 1")),
                () -> assertEquals(3, success.size()),
                () -> assertTrue(success.contains("Task 2 removed from user " +  userDomainTwo.getId())),
                () -> assertEquals("Not found exception. Task with id 98 not found", errors.get(0)),
                () -> assertNull(taskTwo.getUser()),
                () -> assertNull(taskThree.getUser()),
                () -> assertNull(taskFour.getUser())
        );

        verify(userRepository, times(3)).findById(anyLong());
        verify(taskRepository, times(5)).findById(anyLong());
        verify(taskRepository, times(2)).findByUser(any(User.class));
        verify(taskRepository, times(3)).save(any(Task.class));
    }
}