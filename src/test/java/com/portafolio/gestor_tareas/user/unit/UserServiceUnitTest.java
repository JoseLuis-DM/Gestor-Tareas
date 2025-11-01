package com.portafolio.gestor_tareas.user.unit;

import com.portafolio.gestor_tareas.config.infrastructure.SecurityConfig;
import com.portafolio.gestor_tareas.exception.domain.BadRequestException;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.users.application.UserServiceImpl;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserResponseDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserWithPermissionsDTO;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpringUserRepository springUserRepository;

    @Mock
    private SecurityConfig securityConfig;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User inputUser;
    private Set<Permission> newPermissions;
    private User userWithPermissions;
    private User userWithTwoPermissions;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {

        inputUser = new User(
                1L,
                "Test",
                "Example",
                "test@example.com",
                "123456",
                Role.USER,
                new HashSet<>(),
                new ArrayList<>()
        );

        newPermissions = new HashSet<>(Set.of(Permission.TASK_READ, Permission.TASK_WRITE));

        userWithPermissions = new User(
                2L,
                "Test User",
                "Example User",
                "testuser@example.com",
                "654321",
                Role.USER,
                new HashSet<>(Arrays.asList(
                        Permission.TASK_WRITE,
                        Permission.TASK_READ,
                        Permission.TASK_ASSIGN,
                        Permission.TASK_DELETE
                )),
                new ArrayList<>()
        );

        userWithTwoPermissions = new User(
                3L,
                "User Test",
                "User Example",
                "usertest@example.com",
                "321123",
                Role.USER,
                new HashSet<>(Arrays.asList(
                        Permission.TASK_WRITE,
                        Permission.TASK_READ
                )),
                new ArrayList<>()
        );
    }

    /*
        CREATE USER
    */

    // Test that validates the creation of a user
    @Test
    void shouldSaveUserSuccessfully() {

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstname(inputUser.getFirstname());
        userDTO.setLastname(inputUser.getLastname());
        userDTO.setEmail(inputUser.getEmail());
        userDTO.setPassword(inputUser.getPassword());
        userDTO.setRole(inputUser.getRole());

        userEntity = new UserEntity();
        userEntity.setFirstname(inputUser.getFirstname());
        userEntity.setLastname(inputUser.getLastname());
        userEntity.setEmail(inputUser.getEmail());
        userEntity.setPassword(inputUser.getPassword());
        userEntity.setRole(inputUser.getRole());

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                null,
                inputUser.getFirstname(),
                inputUser.getLastname(),
                inputUser.getEmail(),
                inputUser.getRole(),
                inputUser.getPermissions()
        );

        when(userMapper.userDTOToUser(userDTO)).thenReturn(inputUser);
        when(userRepository.existsEmail("test@example.com")).thenReturn(false);
        when(userMapper.userToUserEntity(inputUser)).thenReturn(userEntity);
        when(springUserRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.userEntityToUserResponseDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.register(userDTO);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());

        verify(userMapper, times(1)).userDTOToUser(userDTO);
        verify(userRepository, times(1)).existsEmail("test@example.com");
        verify(userMapper, times(1)).userToUserEntity(inputUser);
        verify(springUserRepository, times(1)).save(userEntity);
        verify(userMapper, times(1)).userEntityToUserResponseDTO(userEntity);
    }

    // Test that validates that a duplicate email cannot be registered
    @Test
    void shouldThrowExceptionWhenEmailAlreadyExist() {

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstname(inputUser.getFirstname());
        userDTO.setLastname(inputUser.getLastname());
        userDTO.setEmail(inputUser.getEmail());
        userDTO.setPassword(inputUser.getPassword());
        userDTO.setRole(inputUser.getRole());

        when(userRepository.existsEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(userDTO));

        verify(userRepository, times(1)).existsEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    /*
        UPDATE USER
     */

    // Test that validates a user's update
    @Test
    void shouldUpdateUserSuccessfully() {

        User updateUser = new User(
                1L,
                "Updated",
                "Example",
                "test@example.com",
                "123456",
                Role.USER,
                Set.of(),
                List.of()
        );

        UserDTO userDTO = new UserDTO();
        userDTO.setId(updateUser.getId());
        userDTO.setFirstname(updateUser.getFirstname());
        userDTO.setLastname(updateUser.getLastname());
        userDTO.setEmail(updateUser.getEmail());
        userDTO.setPassword(updateUser.getPassword());
        userDTO.setRole(updateUser.getRole());

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                userDTO.getId(),
                userDTO.getFirstname(),
                userDTO.getLastname(),
                userDTO.getEmail(),
                userDTO.getRole(),
                userDTO.getPermissions()
        );

        userEntity = new UserEntity();
        userEntity.setId(updateUser.getId());
        userEntity.setFirstname(updateUser.getFirstname());
        userEntity.setLastname(updateUser.getLastname());
        userEntity.setEmail(updateUser.getEmail());
        userEntity.setRole(updateUser.getRole());
        userEntity.setPermissions(updateUser.getPermissions());

        when(userRepository.findById(1L)).thenReturn(Optional.of(inputUser));
        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));
        when(userRepository.existsEmail("test@example.com")).thenReturn(false);
        when(userMapper.userToUserEntity(updateUser)).thenReturn(userEntity);
        when(springUserRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.userEntityToUserResponseDTO(userEntity)).thenReturn(userResponseDTO);

        UserDetails userDetails = mock(UserDetails.class);

        UserResponseDTO result = userService.update(userDTO, userDetails);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstname());

        verify(userRepository, times(1)).findById(inputUser.getId());
        verify(userRepository, times(1)).existsEmail("test@example.com");
        verify(userMapper, times(1)).userToUserEntity(updateUser);
        verify(springUserRepository, times(1)).save(userEntity);
        verify(userMapper, times(1)).userEntityToUserResponseDTO(userEntity);
    }

    // Test that attempts to update a user that does not exist
    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentUser() {

        UserDTO updatedUser = new UserDTO(
                99L,
                "Updated",
                "test@example.com",
                "123456",
                "Full Name",
                Role.USER,
                new ArrayList<>(),
                new HashSet<>()
        );

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserDetails userDetails = mock(UserDetails.class);


        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(updatedUser, userDetails)
        );

        assertNotNull(exception);
        assertEquals("Not found exception. User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any());
    }

    /*
        FIND USER
     */

    // Test that validates that a user was found
    @Test
    void shouldFindUserById() {

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                inputUser.getId(),
                inputUser.getFirstname(),
                inputUser.getLastname(),
                inputUser.getEmail(),
                inputUser.getRole(),
                inputUser.getPermissions()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(inputUser));
        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));
        when(userMapper.userToUserResponseDTO(inputUser)).thenReturn(userResponseDTO);

        UserDetails userDetails = mock(UserDetails.class);

        UserResponseDTO foundUser = userService.findById(1L, userDetails);

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).userToUserResponseDTO(inputUser);
    }

    // Test that validates that a user was not found because it does not exist
    @Test
    void shouldReturnEmptyWhenUserNotFound() {

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        UserDetails userDetails = mock(UserDetails.class);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findById(2L, userDetails)
        );

        assertNotNull(exception);
        assertEquals("Not found exception. User not found", exception.getMessage());

        verify(userRepository, times(1)).findById(2L);
    }

    /*
        DELETE USER
     */

    // Successful user deletion test
    @Test
    void shouldDeleteUserSuccessfully() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(inputUser));
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    // Test to delete a user that does not exist
    @Test
    void shouldThrowExceptionWhenDeletingNonexistentUser() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.delete(99L));

        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).deleteById(any());
    }

    /*
        ADD PERMISSIONS
     */

    // Test to add permissions to a user by email
    @Test
    void shouldAddPermissionsByEmailSuccessfully() {

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(inputUser));
        when(userRepository.save(any(User.class))).thenReturn(inputUser);

        userService.addPermissions(null, "test@example.com", newPermissions);

        assertTrue(inputUser.getPermissions().containsAll(newPermissions));
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(inputUser);
    }

    // Test to add permissions to a user by ID that does not exist
    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.addPermissions(99L, null, newPermissions));

        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any());
    }

    // Test to add permissions to a user when the ID or email are not provided
    @Test
    void shouldThrowExceptionWhenNoIdOrEmailProvide() {

        assertThrows(RuntimeException.class,
                () -> userService.addPermissions(null, null, newPermissions));

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    /*
        DELETE PERMISSIONS
    */

    // Test where all permissions are removed from a user
    @Test
    void shouldDeleteAllPermissionsByUserIdSuccessfully() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithPermissions));
        when(userRepository.save(any(User.class))).thenReturn(userWithPermissions);

        Map<String, Object> response =
                userService.deletePermissions(2L, null, true, null);

        assertAll(
                () -> assertNotNull(response),
                () -> assertThat(userWithPermissions.getPermissions()).isNull(),
                () -> assertNull(response.get("notFound"))
        );

        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).save(userWithPermissions);
    }

    // Test where permissions are removed from a user based on their userId
    @Test
    void shouldDeletePermissionsByUserIdSuccessfully() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithPermissions));
        when(userRepository.save(any(User.class))).thenReturn(userWithPermissions);

        Set<Permission> permissions = new HashSet<>(Arrays.asList(
                Permission.TASK_WRITE,
                Permission.TASK_READ
        ));

        Map<String, Object> response =
                userService.deletePermissions(2L, null, false, permissions);

        assertAll(
                () -> assertNotNull(response),
                () -> assertNotNull(response.get("removed")),
                () -> assertEquals(permissions, response.get("removed"))
        );

        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).save(userWithPermissions);
    }

    // Test where permissions are removed from a user based on their email
    @Test
    void shouldDeletePermissionsByEmailSuccessfully() {

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(userWithPermissions));
        when(userRepository.save(any(User.class))).thenReturn(userWithPermissions);

        Set<Permission> permissions = new HashSet<>(Arrays.asList(
                Permission.TASK_DELETE,
                Permission.TASK_WRITE
        ));

        Map<String, Object> response =
                userService.deletePermissions(null, "testuser@example.com", false, permissions);

        assertAll(
                () -> assertNotNull(response),
                () -> assertNotNull(response.get("removed")),
                () -> assertEquals(permissions, response.get("removed"))
        );

        verify(userRepository, times(1)).findByEmail("testuser@example.com");
        verify(userRepository, times(1)).save(userWithPermissions);
    }

    // Test where permissions are removed from a user who did not have some permissions
    @Test
    void shouldReturnWarningForPermissionsUsersDoesNotHave() {

        when(userRepository.findById(3L)).thenReturn(Optional.of(userWithTwoPermissions));
        when(userRepository.save(any(User.class))).thenReturn(userWithTwoPermissions);

        Set<Permission> permissions = new HashSet<>(Arrays.asList(
                Permission.TASK_DELETE,
                Permission.TASK_WRITE
        ));

        Map<String, Object> response =
                userService.deletePermissions(3L, null, false, permissions);

        assertAll(
                () -> assertNotNull(response),
                () -> assertTrue(response.containsKey("removed")),
                () -> assertTrue(response.containsKey("notFound"))
        );

        verify(userRepository, times(1)).findById(3L);
        verify(userRepository, times(1)).save(userWithTwoPermissions);
    }

    // Test where an attempt was made to remove permissions from a user that does not exist
    @Test
    void shouldReturnNotFoundBecauseNotExistUser() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Set<Permission> permissions = new HashSet<>(Arrays.asList(
                Permission.TASK_DELETE,
                Permission.TASK_WRITE
        ));

        assertThrows(NotFoundException.class,
                () -> userService.deletePermissions(99L, null, false, permissions));

        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any());
    }

    // Test where an attempt was made to remove permissions from a user but with a bad request
    @Test
    void shouldReturnBadRequestDataNotProvided() {

        Set<Permission> permissions = new HashSet<>(Arrays.asList(
                Permission.TASK_DELETE,
                Permission.TASK_WRITE
        ));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.deletePermissions(null, null, false, permissions));

        assertThat(exception.getMessage()).isEqualTo("Bad request exception. UserId or email must be provided");
    }

    /*
        SHOW PERMISSIONS
    */

    // Test that validates that a user's permissions were found
    @Test
    void shouldShowPermissionsSuccessfully() {

        when(userRepository.findById(2L)).thenReturn(Optional.of(userWithPermissions));

        UserDetails userDetails = mock(UserDetails.class);

        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));

        List<Permission> permissions = userService.showPermissions(2L, userDetails);

        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());
        assertEquals(userWithPermissions.getPermissions().size(), permissions.size());

        verify(userRepository, times(1)).findById(2L);
    }

    // Test that validates that a user's permissions are displayed even if they are 0
    @Test
    void shouldShowEmptyPermissionsSuccessfully() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(inputUser));

        UserDetails userDetails = mock(UserDetails.class);

        doNothing().when(securityConfig).checkAccess(anyLong(), any(UserDetails.class));

        List<Permission> permissions = userService.showPermissions(1L, userDetails);

        assertTrue(permissions.isEmpty());
        assertThat(0).isZero();

        verify(userRepository, times(1)).findById(1L);
    }

    /*
        SHOW ALL USERS WITH PERMISSIONS
    */

    // Test that validates that the users with their permissions were found
    @Test
    void shouldShowAllUsersWithPermissionsSuccessfully() {

        UserWithPermissionsDTO dto = new UserWithPermissionsDTO();
        dto.setFirstname("Admin");
        dto.setEmail("admin@test.com");
        dto.setPermissions(Set.of(Permission.TASK_READ, Permission.TASK_WRITE));

        when(userRepository.findAll()).thenReturn(List.of(userWithPermissions));
        when(userMapper.userToUserWithPermissionsDTOs(List.of(userWithPermissions))).thenReturn(List.of(dto));

        UserDetails userDetails = mock(UserDetails.class);

        List<UserWithPermissionsDTO> response = userService.showAllUsersWithPermissions(userDetails);

        verify(securityConfig).checkAdminAccess(userDetails);
        verify(userRepository).findAll();

        assertNotNull(response);
    }

    // Test that validates that there are no users with their permissions
    @Test
    void shouldShowEmptyUsersWithPermissions() {

        UserDetails userDetails = mock(UserDetails.class);

        List<UserWithPermissionsDTO> response = userService.showAllUsersWithPermissions(userDetails);

        verify(securityConfig).checkAdminAccess(userDetails);
        verify(userRepository).findAll();


        assertNotNull(response);
    }
}
