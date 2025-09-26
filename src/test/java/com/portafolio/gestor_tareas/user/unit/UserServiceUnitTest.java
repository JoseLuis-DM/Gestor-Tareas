package com.portafolio.gestor_tareas.user.unit;

import com.portafolio.gestor_tareas.users.application.UserServiceImpl;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.Role;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User inputUser;
    private Set<Permission> newPermissions;

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
    }

    /*
        CREATE USER
    */

    // Test that validates the creation of a user
    @Test
    void shouldSaveUserSuccessfully() {

        when(userRepository.existsEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(inputUser)).thenReturn(inputUser);

        User result = userService.register(inputUser);

        assertEquals(inputUser, result);
        verify(userRepository, times(1)).existsEmail("test@example.com");
        verify(userRepository, times(1)).save(inputUser);
    }

    // Test that validates that a duplicate email cannot be registered
    @Test
    void shouldThrowExceptionWhenEmailAlreadyExist() {

        when(userRepository.existsEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(inputUser));

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

        when(userRepository.findById(1L)).thenReturn(Optional.of(inputUser));
        when(userService.register(updateUser)).thenReturn(updateUser);

        User result = userService.update(updateUser);

        assertEquals("Updated", result.getFirstname());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(updateUser);
    }

    // Test that attempts to update a user that does not exist
    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentUser() {

        User updatedUser = new User(
                99L,
                "Updated",
                "test@example.com",
                "123456",
                "Full Name",
                Role.USER,
                Set.of(),
                List.of()
        );

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.update(updatedUser));
        verify(userRepository, times(1)).findById(99L);
        verify(userRepository, never()).save(any());
    }

    /*
        FIND USER
     */

    // Test that validates that a user was found
    @Test
    void shouldFindUserById() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(inputUser));

        Optional<User> foundUser = userService.findById(1L);

        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    // Test that validates that a user was not found because it does not exist
    @Test
    void shouldReturnEmptyWhenUserNotFound() {

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findById(2L);

        assertFalse(foundUser.isPresent());
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
}
