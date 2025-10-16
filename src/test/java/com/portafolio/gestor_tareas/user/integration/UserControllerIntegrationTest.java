package com.portafolio.gestor_tareas.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portafolio.gestor_tareas.config.TestUserFactory;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
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
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringUserRepository springUserRepository;

    @Autowired
    private TestUserFactory userFactory;

    private TestUserFactory.TestUser regularUser;
    private TestUserFactory.TestUser adminUser;
    private TestUserFactory.TestUser userWithoutPermissions;
    private TestUserFactory.TestUser userWithTwoPermissions;

    private Long userId;
    private List<String> permissions;

    @BeforeEach
    void setUp() throws Exception {

        regularUser = userFactory.createRegularUser();
        adminUser = userFactory.createAdminUser();
        userWithoutPermissions = userFactory.createRegularUserWithoutPermissions();
        userWithTwoPermissions = userFactory.createUserWithTwoPermissions();
    }

    /*
        register (POST)
     */
    // Test that validates the creation of a user
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        UserDTO requestDto = new UserDTO();
        requestDto.setFirstname("Test");
        requestDto.setLastname("User");
        requestDto.setEmail("test.user@example.com");
        requestDto.setPassword("123456");
        requestDto.setRole(Role.USER);
        requestDto.setTask(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.firstname").value("Test"))
                .andExpect(jsonPath("$.data.email").value("test.user@example.com"));
    }

    // Test that validates that a duplicate email cannot be registered
    @Test
    void shouldNotRegisterDuplicateEmail() throws Exception {

        UserDTO requestDto = new UserDTO();
        requestDto.setFirstname("Test");
        requestDto.setLastname("User");
        requestDto.setEmail("usertest@test.com");
        requestDto.setPassword("123456");
        requestDto.setRole(Role.USER);
        requestDto.setTask(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict());
    }

    // Test that invalidates due to bad request by not sending the password
    @Test
    void shouldReturnBadRequestForInvalidData() throws Exception {

        UserDTO requestDto = new UserDTO();
        requestDto.setFirstname("Test");
        requestDto.setLastname("User");
        requestDto.setEmail("usertest@test.com");
        requestDto.setRole(Role.USER);
        requestDto.setTask(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    /*
        update (PUT)
    */
    // Test that validates the update of a user by the same user
    @Test
    void regularUserCanUpdateTheirOwnData() throws Exception {

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        UserDTO requestDto = new UserDTO();
        requestDto.setId(userId);
        requestDto.setFirstname("User updated");
        requestDto.setLastname("Test updated");
        requestDto.setEmail(regularUser.getEmail());
        requestDto.setPassword("123456");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstname").value("User updated"))
                .andExpect(jsonPath("$.data.lastname").value("Test updated"));
    }

    // Test that validates that a user cannot update another user
    @Test
    void userCannotUpdateOtherUser() throws Exception {

        userId = springUserRepository.findByEmail(adminUser.getEmail())
                .orElseThrow()
                .getId();

        UserDTO requestDto = new UserDTO();
        requestDto.setId(userId);
        requestDto.setFirstname("User admin updated");
        requestDto.setLastname("Admin updated");
        requestDto.setEmail(adminUser.getEmail());
        requestDto.setPassword("654321");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    // Test that validates that an admin can update any user
    @Test
    void adminCanUpdateAnyUser() throws Exception {

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        UserDTO requestDto = new UserDTO();
        requestDto.setId(userId);
        requestDto.setFirstname("User updated by admin");
        requestDto.setLastname("Test updated by admin");
        requestDto.setEmail(regularUser.getEmail());
        requestDto.setPassword("123456");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstname").value("User updated by admin"))
                .andExpect(jsonPath("$.data.lastname").value("Test updated by admin"));
    }

    // Test that validates that a user is not updated due to a bad request since the first and last name are empty
    @Test
    void updateInvalidDataShouldReturnBadRequest() throws Exception {

        UserDTO requestDto = new UserDTO();
        requestDto.setId(2L);
        requestDto.setFirstname(null);
        requestDto.setLastname(null);
        requestDto.setEmail(regularUser.getEmail());
        requestDto.setPassword("123456");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    /*
        delete (DELETE)
    */
    // Successful user deletion test
    @Test
    void adminCanDeleteUser() throws Exception {

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", userId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test where a normal user cannot delete another
    @Test
    void userCannotDeleteOtherUser() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", 3L)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // Test where an admin tries to delete a user that does not exist
    @Test
    void deletingNonexistentUserReturnsNotFound() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}", 99L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /*
        findById (GET)
    */

    // Integration test that verifies that an administrator correctly finds a user by their ID
    @Test
    void shouldAdminGetUserByIdSuccessfully() throws Exception {

        userId = springUserRepository.findByEmail("usertest@test.com").get().getId();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Integration test that verifies that an admin cannot find a user who does not exist
    @Test
    void shouldReturnNotFoundForNonexistentUser() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", 999L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Integration procedure that verifies that a user is correctly entered by their ID
    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {

        userId = springUserRepository.findByEmail("usertest@test.com").get().getId();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /*
        findAll (GET)
    */

    // Test where an admin gets all the users
    @Test
    void adminCanGetAllUsers() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Test where a normal user tries to get all users
    @Test
    void userCannotGetAllUsers() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /*
        add permission by id (POST)
    */

    // Test where an admin adds permissions to a user
    @Test
    void adminCanAddPermissionById() throws Exception {

        userId = springUserRepository.findByEmail("usertest@test.com").get().getId();

        permissions = List.of("TASK_DELETE");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isOk());
    }

    // Test to prove that a normal user cannot add permissions to another user
    @Test
    void userCannotAddPermissionById() throws Exception {

        permissions = List.of("TASK_DELETE");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/{id}/permissions", 1L)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isForbidden());
    }

    // Test to try to add permissions to a user that does not exist
    @Test
    void addingPermissionToNonexistentUserReturnsNotFound() throws Exception {

        permissions = List.of("TASK_DELETE");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/{id}/permissions", 99L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isNotFound());
    }

    // Test to try to add permissions to a user but it is a bad request
    @Test
    void addingInvalidPermissionReturnsBadRequest() throws Exception {

        permissions = List.of("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/{id}/permissions", 2L)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isBadRequest());
    }

    /*
        add permissions by email (POST)
    */

    // Test where an admin adds permissions to a user
    @Test
    void adminCanAddPermissionByEmail() throws Exception {

        permissions = List.of("TASK_DELETE");

        mockMvc.perform(MockMvcRequestBuilders.post(
                "/api/users/email/{email}/permissions",
                                "usertest@test.com")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isOk());
    }

    // Test to prove that a normal user cannot add permissions to another user
    @Test
    void userCannotAddPermissionByEmail() throws Exception {

        permissions = List.of("TASK_DELETE");

        mockMvc.perform(MockMvcRequestBuilders.post(
                "/api/users/email/{email}/permissions",
                                "admintest@test.com")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isForbidden());
    }

    // Test to try to add permissions to a user that does not exist
    @Test
    void addingPermissionToNonexistentEmailReturnsNotFound() throws Exception {

        permissions = List.of("TASK_DELETE");

        mockMvc.perform(MockMvcRequestBuilders.post(
                "/api/users/email/{email}/permissions",
                                "test@test.com")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isNotFound());
    }

    // Test to try to add permissions to a user but it is a bad request
    @Test
    void addingInvalidPermissionByEmailReturnsBadRequest() throws Exception {

        permissions = List.of("");

        mockMvc.perform(MockMvcRequestBuilders.post(
                "/api/users/email/{email}/permissions",
                                "usertest@test.com")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isBadRequest());
    }

    /*
        deletePermissionsById (DELETE)
    */

    // Test where all permissions are removed from a user by their userId
    @Test
    void shouldSuccessfullyRemoveAllPermissionsByUserId() throws Exception {

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All permissions removed successfully"));
    }

    // Test where some permissions are removed from a user by their user ID
    @Test
    void shouldSuccessfullyRemoveSomePermissionsByUserId() throws Exception {

        permissions = List.of("TASK_DELETE", "TASK_WRITE");

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permissions removed successfully"));
    }

    // Test where you try to remove some permissions and not others because the user does not have them
    @Test
    void shouldSuccessfullyRemoveExistingPermissionsByUserId() throws Exception {

        permissions = List.of("TASK_DELETE", "TASK_WRITE");

        userId = springUserRepository.findByEmail(userWithTwoPermissions.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Some permissions were not found for this user"));
    }

    // Test where you try to remove permissions from a user who does not have permissions
    @Test
    void shouldReturnConflictBecauseUserDoesNotPermissionsByUserId() throws Exception {

        permissions = List.of("TASK_DELETE", "TASK_WRITE");

        userId = springUserRepository.findByEmail(userWithoutPermissions.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isConflict());
    }

    // Test where an attempt is made to remove permissions from a user that does not exist
    @Test
    void shouldReturnNotFoundDueNonExistentUserByUserId() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}/permissions", 99L)
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Test where an attempt was made to remove permissions from a user with a bad request
    @Test
    void shouldReturnBadRequestForEmptyOrNullPermissionsByUserId() throws Exception {

        permissions = List.of();

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isBadRequest());
    }

    // Test where a regular user attempts to remove permissions from another user
    @Test
    void shouldReturnForbiddenBecauseIsNotAdminByUserId() throws Exception {

        userId = springUserRepository.findByEmail(userWithTwoPermissions.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/{id}/permissions", userId)
                        .header("Authorization", regularUser.getToken())
                        .param("allPermissions", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /*
        deletePermissionsByEmail (DELETE)
    */

    // Test where all permissions are removed from a user by their email
    @Test
    void shouldSuccessfullyRemoveAllPermissionsByEmail() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete(
                "/api/users/email/{email}/permissions",
                                "usertest@test.com")
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All permissions removed successfully"));
    }

    // Test where some permissions are removed from a user by their email
    @Test
    void shouldSuccessfullyRemoveSomePermissionsByEmail() throws Exception {

        permissions = List.of("TASK_DELETE", "TASK_WRITE");

        mockMvc.perform(MockMvcRequestBuilders.delete(
                "/api/users/email/{email}/permissions",
                                "usertest@test.com")
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permissions removed successfully"));
    }

    // Test where you try to remove some permissions and not others because the user does not have them
    @Test
    void shouldSuccessfullyRemoveExistingPermissionsByEmail() throws Exception {

        permissions = List.of("TASK_DELETE", "TASK_WRITE");

        mockMvc.perform(MockMvcRequestBuilders.delete(
                "/api/users/email/{email}/permissions",
                                "testuser@test.com")
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(
                        "Some permissions were not found for this user"));
    }

    // Test where you try to remove permissions from a user who does not have permissions
    @Test
    void shouldReturnConflictBecauseUserDoesNotPermissionsByEmail() throws Exception {

        permissions = List.of("TASK_DELETE", "TASK_WRITE");

        mockMvc.perform(MockMvcRequestBuilders.delete(
                "/api/users/email/{email}/permissions",
                                "user@test.com")
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isConflict());
    }

    // Test where an attempt is made to remove permissions from a user that does not exist
    @Test
    void shouldReturnNotFoundDueNonExistentUserByEmail() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete(
                "/api/users/email/{email}/permissions",
                                "notfound@test.com")
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Test where an attempt was made to remove permissions from a user with a bad request
    @Test
    void shouldReturnBadRequestForEmptyOrNullPermissionsByEmail() throws Exception {

        permissions = List.of();

        mockMvc.perform(MockMvcRequestBuilders.delete(
                "/api/users/email/{email}/permissions",
                                "usertest@test.com")
                        .header("Authorization", adminUser.getToken())
                        .param("allPermissions", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissions)))
                .andExpect(status().isBadRequest());
    }

    // Test where a regular user attempts to remove permissions from another user
    @Test
    void shouldReturnForbiddenBecauseIsNotAdminByEmail() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete(
                "/api/users/email/{email}/permissions",
                                "testuser@test.com")
                        .header("Authorization", regularUser.getToken())
                        .param("allPermissions", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /*
        showPermissions (GET)
    */

    // Test that validates that an admin found the permissions of a user
    @Test
    void shouldShowPermissionsToAdmin() throws Exception {

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permissions found"));
    }

    // Test that validates that a user has found their permissions
    @Test
    void shouldShowPermissionsToUser() throws Exception {

        userId = springUserRepository.findByEmail(regularUser.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}/permissions", userId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Permissions found"));
    }

    // Test that validates that the permissions of a user who does not have permissions were not found
    @Test
    void shouldReturnEmptyBecauseNotPermits() throws Exception {

        userId = springUserRepository.findByEmail(userWithoutPermissions.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}/permissions", userId)
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User has no assigned permissions"));
    }

    // Test where a normal user tries to see the permissions of another user
    @Test
    void shouldReturnForbiddenBecauseNotUserId() throws Exception {

        userId = springUserRepository.findByEmail(userWithoutPermissions.getEmail())
                .orElseThrow()
                .getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}/permissions", userId)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // Test where an admin tries to see the permissions of a user that does not exist
    @Test
    void shouldReturnNotFoundBecauseUserDoesNotExist() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}/permissions", 99L)
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /*
        showAllUsersWithPermissions (GET)
    */

    // Test that validates that an administrator found the users with their permissions
    @Test
    void shouldShowAllUsersWithPermissionsToAdmin() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/permissions")
                        .header("Authorization", adminUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users with permissions found"));
    }

    // Test where a normal user tries to view user permissions
    @Test
    void shouldReturnForbiddenBecauseIsNotAdmin() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/permissions")
                        .header("Authorization", regularUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
