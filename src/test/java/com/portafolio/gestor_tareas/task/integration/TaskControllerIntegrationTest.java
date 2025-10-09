package com.portafolio.gestor_tareas.task.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portafolio.gestor_tareas.config.TestTaskFactory;
import com.portafolio.gestor_tareas.config.TestUserFactory;
import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.infrastructure.dto.TaskDTO;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.task.infrastructure.repository.SpringTaskRepository;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringTaskRepository springTaskRepository;

    @Autowired
    private SpringUserRepository springUserRepository;

    @Autowired
    private TestUserFactory userFactory;

    @Autowired
    private TestTaskFactory testTaskFactory;

    private TestUserFactory.TestUser regularUser;
    private TestUserFactory.TestUser adminUser;
    private TestUserFactory.TestUser userWithoutPermission;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() throws Exception {

        regularUser = userFactory.createRegularUser();
        adminUser = userFactory.createAdminUser();
        userWithoutPermission = userFactory.createRegularUserWithoutPermissions();

        userEntity = new UserEntity();
        userEntity.setFirstname("Test User");
        userEntity.setEmail("test@example.com");
        userEntity.setPassword("123456");

        springUserRepository.save(userEntity);
    }

    private Long createTaskAndGetId(UserEntity user, String title) {

        TaskEntity task = testTaskFactory.createTask(user, title);
        return task.getId();
    }

    private TaskDTO createTaskDTO(String title, String description, boolean completed) {

        return testTaskFactory.createTaskDTO(title, description, completed);
    }

    private TaskDTO createTaskDTOWithId(Long id, String title, String description, boolean completed) {

        return testTaskFactory.createTaskDTOWithId(id, title, description, completed);
    }

    /*
        register (POST)
     */

    // Test that validates the creation of a task for admin
    @Test
    void shouldRegisterTaskSuccessfullyByAdmin() throws Exception {

        TaskDTO task = createTaskDTO("Task Admin", "Task by admin", false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Task Admin"));
    }

    // Test that validates the creation of a task for user with permission(TASK_WRITE)
    @Test
    void shouldRegisterTaskSuccessfullyByUser() throws Exception {

        TaskDTO task = createTaskDTO("Task User", "Task by user", false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Task User"));
    }

    // Test that invalidates the creation of the task by a user without permissions
    @Test
    void shouldNotRegisterTaskUserWithoutPermissions() throws Exception {

        TaskDTO task = createTaskDTO("Task User", "Task by user without permission", false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", userWithoutPermission.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isForbidden());
    }

    // Test that validates that a task with a duplicate title and duplicated userId cannot be registered
    @Test
    void shouldNotRegisterDuplicateTitleAndUserID() throws Exception {

        TaskDTO task = createTaskDTO("Task User", "Task by user", false);

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

        TaskDTO requestDto = createTaskDTO(null, "Task by admin", false);

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

        Long taskId = createTaskAndGetId(userEntity, "Task to update");

        TaskDTO task = createTaskDTOWithId(taskId, "Updated title", "Updated description", false);

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

        TaskDTO task = createTaskDTO("Task user", "Task by user", false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        Long userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        Long taskId = springTaskRepository.findByUserIdAndTitleIgnoreCase(userId, "Task user")
                .orElseThrow()
                .getId();

        TaskDTO taskDTO = createTaskDTOWithId(taskId, "Updated task", "Updated description", false);

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

        Long taskId = createTaskAndGetId(userEntity, "Task");

        TaskDTO task = createTaskDTOWithId(taskId, "Updated title", "Updated description", false);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isForbidden());
    }

    // Test where a task that does not exist cannot be updated
    @Test
    void updatedNoExistingTaskReturnNotFound() throws Exception {

        TaskDTO task = createTaskDTOWithId(999L, "Updated title", "Updated description", false);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound());
    }

    // Test that validates that a user does not update a task due to a bad request since the title and description are empty
    @Test
    void updateInvalidDataShouldReturnBadRequest() throws Exception {

        Long taskId = createTaskAndGetId(userEntity, "Task");

        TaskDTO task = createTaskDTOWithId(taskId, null, null, false);

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

        Long taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/task/{id}", taskId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Testing successful deletion of a task by its user
    @Test
    void userCanDeleteOwnTask() throws Exception {

        TaskDTO task = createTaskDTO("Task user", "Task by user", false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        Long userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        Long taskId = springTaskRepository.findByUserIdAndTitleIgnoreCase(userId, "Task user")
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

        Long taskId = createTaskAndGetId(userEntity, "Task");

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

        Long taskId = createTaskAndGetId(userEntity, "Task");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/{id}", taskId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Integration procedure that verifies that a user finds their task by task ID
    @Test
    void shouldGetUserTaskByIdSuccessfully() throws Exception {

        TaskDTO task = createTaskDTO("Task user", "Task by user", false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/task")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        Long userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        Long taskId = springTaskRepository.findByUserIdAndTitleIgnoreCase(userId, "Task user")
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/{id}", taskId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Integration test that verifies that an admin cannot find a task who does not exist
    @Test
    void shouldReturnNotFoundForNonexistentTask() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/{id}", 999L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Test where a user cannot find a task that is not theirs
    @Test
    void userCannotFindOtherTaskThatNotHis() throws Exception {

        Long taskId = createTaskAndGetId(userEntity, "Task");

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
}
