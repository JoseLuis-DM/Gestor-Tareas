package com.portafolio.gestor_tareas.task.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portafolio.gestor_tareas.config.TestTaskFactory;
import com.portafolio.gestor_tareas.config.TestUserFactory;
import com.portafolio.gestor_tareas.config.infrastructure.SecurityUtils;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.task.domain.TaskRepository;
import com.portafolio.gestor_tareas.task.infrastructure.dto.BulkTaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskAssignmentDTO;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringTaskRepository springTaskRepository;

    @Autowired
    private SpringUserRepository springUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestUserFactory userFactory;

    @Autowired
    private TestTaskFactory testTaskFactory;

    @SuppressWarnings("removal")
    @MockBean
    private SecurityUtils securityUtils;

    private TestUserFactory.TestUser regularUser;
    private TestUserFactory.TestUser adminUser;
    private TestUserFactory.TestUser userWithoutPermission;
    private UserEntity userEntity;
    private TaskEntity taskOne;
    private TaskEntity taskTwo;
    private TaskEntity taskThree;
    private TaskEntity taskFour;
    private TaskEntity taskFive;
    private BulkTaskDTO bulkTaskDTO;

    private Long taskId;
    private List<Long> taskIds;

    @BeforeEach
    void setUp() throws Exception {

        springTaskRepository.deleteAll();
        springUserRepository.deleteAll();

        regularUser = userFactory.createRegularUser();
        adminUser = userFactory.createAdminUser();
        userWithoutPermission = userFactory.createRegularUserWithoutPermissions();

        userEntity = new UserEntity();
        userEntity.setFirstname("Test User");
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("123456");

        springUserRepository.save(userEntity);

        taskOne = new TaskEntity();
        taskOne.setTitle("Test One");
        taskOne.setDescription("Sample test one");
        taskOne.setCompleted(false);

        springTaskRepository.save(taskOne);

        taskTwo = new TaskEntity();
        taskTwo.setTitle("Test Two");
        taskTwo.setDescription("Sample test two");
        taskTwo.setCompleted(false);

        springTaskRepository.save(taskTwo);

        taskThree = new TaskEntity();
        taskThree.setTitle("Test Three");
        taskThree.setDescription("Sample test three");
        taskThree.setCompleted(false);

        springTaskRepository.save(taskThree);

        taskFour = new TaskEntity();
        taskFour.setTitle("Test Four");
        taskFour.setDescription("Sample test four");
        taskFour.setCompleted(false);

        springTaskRepository.save(taskFour);

        taskFive = new TaskEntity();
        taskFive.setTitle("Test Five");
        taskFive.setDescription("Sample test five");
        taskFive.setCompleted(false);

        springTaskRepository.save(taskFive);
    }

    private Long createTaskAndGetId(UserEntity user, String title) {

        TaskEntity task = testTaskFactory.createTask(user, title);
        return task.getId();
    }

    private TaskDTO createTaskDTO(String title, String description) {

        return testTaskFactory.createTaskDTO(title, description);
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
        register (POST)
     */

    // Test that validates the creation of a task without being assigned to a user
    @Test
    void shouldSuccessfullyRegisterTaskWithoutAssigningUser() throws Exception {

        TaskDTO task = createUnassignedTaskDTO("Task Admin", "Task by admin");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Task Admin"))
                .andExpect(jsonPath("$.data.userId", nullValue()));
    }

    // Test that validates the creation of a task and its assignment to a user
    @Test
    void shouldSuccessfullyRegisterTaskWithAssignedUser() throws Exception {

        TaskDTO task = createAssingedTaskDTO("Task Admin", "Task by admin", userEntity.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Task Admin"))
                .andExpect(jsonPath("$.data.userId", notNullValue()));
    }

    // Test that validates the creation of a task assigned to the logged-in user
    @Test
    void shouldRegisterTaskSuccessfullyWithUserLogged() throws Exception {

        when(securityUtils.getCurrentUserId()).thenReturn(adminUser.getUserID());

        TaskDTO task = createOwnTask("Task Admin", "Task by admin");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Task Admin"))
                .andExpect(jsonPath("$.data.userId", notNullValue()));
    }

    // Test that validates the creation of a task for user with permission(TASK_WRITE)
    @Test
    void shouldRegisterTaskSuccessfullyByUser() throws Exception {

        TaskDTO task = createUnassignedTaskDTO("Task User", "Task by user");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());
    }

    // Test that invalidates the creation of the task by a user without permissions
    @Test
    void shouldNotRegisterTaskUserWithoutPermissions() throws Exception {

        TaskDTO task = createTaskDTO("Task User", "Task by user without permission");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", userWithoutPermission.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isForbidden());
    }

    // Test that validates that a task with a duplicate title and duplicated userId cannot be registered
    @Test
    void shouldNotRegisterDuplicateTitleAndUserID() throws Exception {

        when(securityUtils.getCurrentUserId()).thenReturn(adminUser.getUserID());

        TaskDTO task = createOwnTask("Task User", "Task by user");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isConflict());
    }

    // Test that invalidates due to bad request by not sending the title
    @Test
    void shouldReturnBadRequestForInvalidData() throws Exception {

        TaskDTO requestDto = createTaskDTO(null, "Task by admin");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    /*
        update (PUT)
    */

    // Test where admin updates any task
    @Test
    void adminUpdateAnyTask() throws Exception {

        when(securityUtils.getCurrentUserId()).thenReturn(adminUser.getUserID());

        taskId = createTaskAndGetId(userEntity, "Task to update");

        TaskDTO task = createTaskDTOWithId(taskId, "Updated title", "Updated description", null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated title"));
    }

    // Test that validates the update of a task by the user who created it
    @Test
    void regularUserCanUpdateTheirOwnTask() throws Exception {

        when(securityUtils.getCurrentUserId()).thenReturn(regularUser.getUserID());

        TaskDTO task = createOwnTask("Task user", "Task by user");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        Long currentUserId = securityUtils.getCurrentUserId();

        taskId = springTaskRepository.findByUserIdAndTitleIgnoreCase(currentUserId, "Task user")
                .orElseThrow()
                .getId();

        TaskDTO taskDTO = createTaskDTOWithId(taskId, "Updated task", "Updated description", currentUserId);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated task"));
    }

    // Test that validates that a user cannot update another user's task
    @Test
    void userCannotUpdateOtherTaskThatNotHis() throws Exception {

        when(securityUtils.getCurrentUserId()).thenReturn(regularUser.getUserID());

        taskId = createTaskAndGetId(userEntity, "Task");

        TaskDTO task = createTaskDTOWithId(taskId, "Updated title", "Updated description", null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isForbidden());
    }

    // Test where a task that does not exist cannot be updated
    @Test
    void updatedNoExistingTaskReturnNotFound() throws Exception {

        TaskDTO task = createTaskDTOWithId(999L, "Updated title", "Updated description", null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound());
    }

    // Test that validates that a user does not update a task due to a bad request since the title and description are empty
    @Test
    void updateInvalidDataShouldReturnBadRequest() throws Exception {

        taskId = createTaskAndGetId(userEntity, "Task");

        TaskDTO task = createTaskDTOWithId(taskId, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isBadRequest());
    }

    /*
        delete (DELETE)
    */

    // Test of successful task deletion by an admin
    @Test
    void adminCanDeleteTask() throws Exception {

        taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/{id}", taskId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Testing successful deletion of a task by its user
    @Test
    void userCanDeleteOwnTask() throws Exception {

        when(securityUtils.getCurrentUserId()).thenReturn(regularUser.getUserID());

        TaskDTO task = createOwnTask("Task user", "Task by user");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        Long userId = securityUtils.getCurrentUserId();

        taskId = springTaskRepository.findByUserIdAndTitleIgnoreCase(userId, "Task user")
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/{id}", taskId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test where a user cannot delete a task that is not theirs
    @Test
    void userCannotDeleteOtherTaskThatNotHis() throws Exception {

        taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/{id}", taskId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // Test where a task that does not exist cannot be deleted
    @Test
    void deleteNoExistingTaskReturnNotFound() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/{id}", 99L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /*
        findById (GET)
    */

    // Integration test that verifies that an administrator correctly finds a task by their ID
    @Test
    void shouldAdminGetTaskByIdSuccessfully() throws Exception {

        taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/{id}", taskId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Integration procedure that verifies that a user finds their task by task ID
    @Test
    void shouldGetUserTaskByIdSuccessfully() throws Exception {

        when(securityUtils.getCurrentUserId()).thenReturn(regularUser.getUserID());

        TaskDTO task = createOwnTask("Task user", "Task by user");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        Long userId = securityUtils.getCurrentUserId();

        taskId = springTaskRepository.findByUserIdAndTitleIgnoreCase(userId, "Task user")
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/{id}", taskId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Integration test that verifies that an admin cannot find a task who does not exist
    @Test
    void shouldReturnNotFoundForNonExistentTask() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/{id}", 999L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Test where a user cannot find a task that is not theirs
    @Test
    void userCannotFindOtherTaskThatNotHis() throws Exception {

        taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/{id}", taskId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /*
        findAll (GET)
    */

    // Test where an admin gets all the tasks
    @Test
    void adminCanGetAllTasks() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test where a normal user gets only his tasks
    @Test
    void userGetTheirTasks() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /*
        updateCompletionStatus (PATCH)
    */

    // Test where an administrator updates the completion of a task
    @Test
    void adminUpdateCompleteAnyTask() throws Exception {

        taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/task/{id}/complete", taskId)
                        .header("Authorization", adminUser.getToken())
                        .param("completed", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test where you try to update a task that does not exist
    @Test
    void shouldReturnNotFoundForNonExistTask() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/task/{id}/complete", 99L)
                        .header("Authorization", adminUser.getToken())
                        .param("completed", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Test where it attempts to update to not completed but is not yet completed
    @Test
    void shouldReturnConflictDueStatusError() throws Exception {

        taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/task/{id}/complete", taskId)
                        .header("Authorization", adminUser.getToken())
                        .param("completed", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    /*
        addTasksToUser (POST)
    */

    // Adding a task successfully
    @Test
    void shouldAddTaskToUserSuccessfully() throws Exception {

        taskIds = List.of(taskOne.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task assigned successfully"));
    }

    // Adding multiple tasks correctly
    @Test
    void shouldAddMultipleTasksToUserSuccessfully() throws Exception {

        taskIds = List.of(taskTwo.getId(), taskThree.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All tasks assigned successfully"));
    }

    // Some tasks are added and others are not because they do not exist
    @Test
    void shouldSaveExistingTasks() throws Exception {

        taskIds = List.of(taskFour.getId(), 99L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some assignments failed"));
    }

    // Problem due to non-existent user
    @Test
    void shouldReturnNotFoundBecauseUserDoesNotExist() throws Exception {

        taskIds = List.of(taskFive.getId(), taskOne.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users/{userId}", 99L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isNotFound());
    }

    // Error when the only task to add does not exist
    @Test
    void shouldReturnNotFoundBecauseTaskDoesNotExist() throws Exception {

        taskIds = List.of(99L);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isNotFound());
    }

    // Error when sending a bad request because no tasks are sent
    @Test
    void shouldReturnBadRequestBecauseTasksAreEmpty() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isBadRequest());
    }

    // Error when a user without permissions tries to assign the task to another user
    @Test
    void shouldReturnForbiddenBecauseUserDoesNotHavePermissions() throws Exception {

        taskIds = List.of(taskFive.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", userWithoutPermission.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isForbidden());
    }

    /*
        addTasksToUsers (POST)
    */

    // Aggregation of multiple tasks to multiple users
    @Test
    void shouldAddMultipleTaskTMultipleUserSuccessfully() throws Exception {

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskOne.getId(), taskThree.getId())),
                        new TaskAssignmentDTO(userWithoutPermission.getUserID(), List.of(taskTwo.getId(), taskFive.getId()))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users")
                .header("Authorization", adminUser.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All tasks assigned successfully"));
    }

    // Adding tasks to existing users
    @Test
    void shouldAddTaskToExistingUsers() throws Exception {

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskFour.getId(), taskThree.getId())),
                        new TaskAssignmentDTO(99L, List.of(taskTwo.getId(), taskFive.getId()))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some assignments failed"));
    }

    // Adding tasks existing to users
    @Test
    void shouldAddTaskExistingToUsers() throws Exception {

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskOne.getId(), 98L)),
                        new TaskAssignmentDTO(userWithoutPermission.getUserID(), List.of(taskTwo.getId(), 99L))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some assignments failed"));
    }

    // Adding existing tasks to existing users
    @Test
    void shouldAddExistingTaskToExistingUsers() throws Exception {

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskOne.getId(), 98L)),
                        new TaskAssignmentDTO(userWithoutPermission.getUserID(), List.of(taskTwo.getId(), 99L))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some assignments failed"));
    }

    /*
        unassignTasksFromUser (DELETE)
    */

    // Unassign a task successfully
    @Test
    void shouldUnassignTaskToUserSuccessfully() throws Exception {

        taskOne.setUser(springUserRepository.findById(regularUser.getUserID()).orElseThrow());
        springTaskRepository.save(taskOne);

        taskIds = List.of(taskOne.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task removed successfully"));
    }

    // Unassign multiple tasks correctly
    @Test
    void shouldUnassignMultipleTaskToUserSuccessfully() throws Exception {

        UserEntity user = springUserRepository.findById(regularUser.getUserID())
                        .orElseThrow(() -> new NotFoundException("User not found"));

        taskOne.setUser(user);
        taskTwo.setUser(user);
        taskThree.setUser(user);
        springTaskRepository.saveAll(List.of(taskOne, taskTwo, taskThree));

        taskIds = List.of(taskOne.getId(), taskTwo.getId(), taskThree.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All tasks successfully unassigned"));
    }

    // Some tasks are unassign and others are not because they do not exist
    @Test
    void shouldUnassignExistingTasks() throws Exception {

        UserEntity user = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskTwo.setUser(user);
        taskThree.setUser(user);
        springTaskRepository.saveAll(List.of(taskOne, taskTwo, taskThree));

        taskIds = List.of(taskTwo.getId(), taskThree.getId(), 98L, 99L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some tasks could not be unassigned"));
    }

    // Some tasks are unassigned and others are not because they are not assigned to the user
    @Test
    void shouldUnassignTasksAssignedToUser() throws Exception {

        UserEntity user = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskFour.setUser(user);
        taskFive.setUser(user);
        springTaskRepository.saveAll(List.of(taskFour, taskFive));

        taskIds = List.of(taskFour.getId(), taskFive.getId(), taskTwo.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some tasks could not be unassigned"));
    }

    // Problem due to non-existent user
    @Test
    void shouldReturnNotFoundBecauseUserNotExist() throws Exception {

        taskIds = List.of(taskOne.getId(), taskTwo.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", 99L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isNotFound());
    }

    // Error when the task to be unassigned is not assigned to the user
    @Test
    void shouldReturnConflictBecauseTaskIsNotAssigned() throws Exception {

        taskIds = List.of(taskOne.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isConflict());
    }

    // Error when some tasks to be unassigned are not assigned to the user
    @Test
    void shouldReturnConflictBecauseSomeTaskIsNotAssigned() throws Exception {

        UserEntity user = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskTwo.setUser(user);
        taskFive.setUser(user);
        springTaskRepository.saveAll(List.of(taskTwo, taskFive));

        taskIds = List.of(taskTwo.getId(), taskFive.getId(), taskFour.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some tasks could not be unassigned"));
    }

    // Error when sending a bad request because no tasks are sent
    @Test
    void shouldBadRequestBecauseTasksAreEmpty() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isBadRequest());
    }

    // Error when a user without permissions tries to assign the task to another user
    @Test
    void shouldForbiddenBecauseUserDoesNotHavePermissions() throws Exception {

        UserEntity user = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskOne.setUser(user);
        taskTwo.setUser(user);
        taskThree.setUser(user);
        springTaskRepository.saveAll(List.of(taskOne, taskTwo, taskThree));

        taskIds = List.of(taskOne.getId(), taskTwo.getId(), taskThree.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users/{userId}", regularUser.getUserID())
                        .header("Authorization", userWithoutPermission.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isForbidden());
    }

    /*
        unassignTasksFromUsers (DELETE)
    */

    // Unassign of multiple tasks to multiple users
    @Test
    void shouldUnassignMultipleTasksFromMultipleUsersSuccessfully() throws Exception {

        UserEntity userOne = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserEntity userTwo = springUserRepository.findById(userWithoutPermission.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskTwo.setUser(userOne);
        taskThree.setUser(userOne);
        taskFour.setUser(userTwo);
        taskFive.setUser(userTwo);
        springTaskRepository.saveAll(List.of(taskTwo, taskThree, taskFour, taskFive));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskTwo.getId(), taskThree.getId())),
                        new TaskAssignmentDTO(userWithoutPermission.getUserID(), List.of(taskFour.getId(), taskFive.getId()))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All tasks successfully unassigned"));
    }

    // Unassign tasks to existing users
    @Test
    void shouldUnassignTaskToExistingUsers() throws Exception {

        UserEntity user = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskTwo.setUser(user);
        taskThree.setUser(user);
        springTaskRepository.saveAll(List.of(taskTwo, taskThree));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskTwo.getId(), taskThree.getId())),
                        new TaskAssignmentDTO(99L, List.of(taskFour.getId(), taskFive.getId()))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some tasks could not be unassigned"));
    }

    // Unassign tasks existing to users
    @Test
    void shouldUnassignTaskExistingToUsers() throws Exception {

        UserEntity userOne = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserEntity userTwo = springUserRepository.findById(userWithoutPermission.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskThree.setUser(userOne);
        taskFive.setUser(userTwo);
        springTaskRepository.saveAll(List.of(taskThree, taskFive));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskThree.getId(), 98L)),
                        new TaskAssignmentDTO(userWithoutPermission.getUserID(), List.of(taskFive.getId(), 99L))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some tasks could not be unassigned"));
    }

    // Unassign existing tasks to existing users
    @Test
    void shouldUnassignExistingTaskToExistingUsers() throws Exception {

        UserEntity userOne = springUserRepository.findById(regularUser.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserEntity userTwo = springUserRepository.findById(userWithoutPermission.getUserID())
                .orElseThrow(() -> new NotFoundException("User not found"));

        taskThree.setUser(userOne);
        taskFour.setUser(userTwo);
        springTaskRepository.saveAll(List.of(taskTwo, taskThree, taskFour, taskFive));

        bulkTaskDTO = new BulkTaskDTO(
                List.of(
                        new TaskAssignmentDTO(regularUser.getUserID(), List.of(taskThree.getId(), 97L)),
                        new TaskAssignmentDTO(userWithoutPermission.getUserID(), List.of(taskFour.getId(), 98L)),
                        new TaskAssignmentDTO(99L, List.of(taskFive.getId(), 99L))
                )
        );

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Some tasks could not be unassigned"));
    }
}
