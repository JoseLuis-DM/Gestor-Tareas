package com.portafolio.gestor_tareas.users.application;

import com.portafolio.gestor_tareas.config.infrastructure.SecurityConfig;
import com.portafolio.gestor_tareas.dto.ApiResponseDTO;
import com.portafolio.gestor_tareas.dto.ApiResponseFactory;
import com.portafolio.gestor_tareas.exception.domain.BadRequestException;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.exception.domain.UserAlreadyExistsException;
import com.portafolio.gestor_tareas.exception.domain.UserDontHavePermissionsException;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.domain.UserService;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserWithPermissionsDTO;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;
    private final UserMapper userMapper;

    private static String notFound = "User not found";

    private User user;

    private boolean existUser;

    @Override
    public User register(User user) {

        existUser = userRepository.existsEmail(user.getEmail());

        if (existUser) {
            throw new UserAlreadyExistsException("The email is already registered");
        }

        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        User updateUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException(notFound));

        existUser = userRepository.existsEmail(user.getEmail());

        if (!updateUser.getEmail().equals(user.getEmail()) &&
                existUser) {
            throw new UserAlreadyExistsException("The email is already registered by another user");
        }

        if (user.getFirstname() == null || user.getFirstname().isEmpty()
                || user.getLastname() == null || user.getLastname().isEmpty()) {
            throw new BadRequestException("The first and last name cannot be empty or null");
        }

        updateUser.setFirstname(user.getFirstname());
        updateUser.setLastname(user.getLastname());
        updateUser.setEmail(user.getEmail());
        updateUser.setPassword(user.getPassword());

        return userRepository.save(updateUser);
    }

    @Override
    public Optional<User> findById(Long id) throws NotFoundException {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(Long id) {

        user = userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("The user does not exist"));
        userRepository.deleteById(id);
    }

    @Transactional
    public void addPermissions(Long userId, String email, Set<Permission> permissions) {

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(notFound));
        } else if (email != null) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException(notFound));
        } else {
            throw new BadRequestException("UserId or email must be provided");
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new BadRequestException("Permissions must be provided");
        }

        user.getPermissions().addAll(permissions);

        userRepository.save(user);
    }

    @Transactional
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> deletePermissions(
            Long userId,
            String email,
            boolean allPermissions,
            Set<Permission> permissions
    ) {

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(notFound));

        } else if (email != null) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException(notFound));

        } else {
            throw new BadRequestException("UserId or email must be provided");
        }

        if (allPermissions) {
            user.setPermissions(null);
            userRepository.save(user);
            return ApiResponseFactory.success(null, "All permissions removed successfully");
        } else {
            return deleteSpecificPermissions(permissions, user);
        }
    }

    @Transactional
    public List<Permission> showPermissions(Long userId, UserDetails userDetails) {

        user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(notFound));

        securityConfig.checkAccess(user.getId(), userDetails);

        return new ArrayList<>(user.getPermissions());
    }

    @Override
    public ResponseEntity<ApiResponseDTO<List<UserWithPermissionsDTO>>> showAllUsersWithPermissions(
            UserDetails userDetails
    ) {

        securityConfig.checkAdminAccess(userDetails);

        List<User> users = userRepository.findAll();

        List<UserWithPermissionsDTO> usersDTO = userMapper.userToUserWithPermissionsDTOs(users);

        if (usersDTO.isEmpty()) {
            return ApiResponseFactory.noContent("No users with assigned permissions were found");
        }

        return ApiResponseFactory.success(usersDTO, "Users with permissions found");
    }

    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> deleteSpecificPermissions(
            Set<Permission> permissions,
            User user
    ) {

        if (permissions == null || permissions.isEmpty()) {
            throw new BadRequestException("The permission list cannot be empty or null.");
        }

        Set<Permission> permissionsUser = new HashSet<>(user.getPermissions());
        Set<Permission> notFoundPermissions = new HashSet<>();
        Set<Permission> removedPermissions = new HashSet<>();

        if (permissionsUser == null || permissionsUser.isEmpty()) {
            throw new UserDontHavePermissionsException("The user does not have permissions");
        }

        for (Permission permission : permissions) {
            if (permissionsUser.remove(permission)) {
                removedPermissions.add(permission);
            } else {
                notFoundPermissions.add(permission);
            }
        }

        user.setPermissions(permissionsUser);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();

        response.put("removed", removedPermissions);
        response.put("notFound", notFoundPermissions);

        if (!notFoundPermissions.isEmpty()) {
            return ApiResponseFactory.warning(response, "Some permissions were not found for this user");
        }

        return ApiResponseFactory.success(response, "Permissions removed successfully");
    }
}
