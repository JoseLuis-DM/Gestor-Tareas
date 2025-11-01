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
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserResponseDTO;
import com.portafolio.gestor_tareas.users.infrastructure.dto.UserWithPermissionsDTO;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
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
    private final SpringUserRepository springUserRepository;
    private final SecurityConfig securityConfig;
    private final UserMapper userMapper;

    private static final String NOT_FOUND = "User not found";

    private User user;

    private boolean existUser;

    @Override
    public UserResponseDTO register(UserDTO userDTO) {

        user = userMapper.userDTOToUser(userDTO);

        existUser = userRepository.existsEmail(userDTO.getEmail());

        if (existUser) {
            throw new UserAlreadyExistsException("The email is already registered");
        }

        UserEntity savedEntity = springUserRepository.save(userMapper.userToUserEntity(user));

        return userMapper.userEntityToUserResponseDTO(savedEntity);
    }

    @Override
    public UserResponseDTO update(UserDTO userDTO, UserDetails userDetails) {

        User updateUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new NotFoundException(NOT_FOUND));

        securityConfig.checkAccess(userDTO.getId(), userDetails);

        existUser = userRepository.existsEmail(userDTO.getEmail());

        if (!updateUser.getEmail().equals(userDTO.getEmail()) &&
                existUser) {
            throw new UserAlreadyExistsException("The email is already registered by another user");
        }

        if (userDTO.getFirstname() == null || userDTO.getFirstname().isEmpty()
                || userDTO.getLastname() == null || userDTO.getLastname().isEmpty()) {
            throw new BadRequestException("The first and last name cannot be empty or null");
        }

        updateUser.setFirstname(userDTO.getFirstname());
        updateUser.setLastname(userDTO.getLastname());
        updateUser.setEmail(userDTO.getEmail());
        updateUser.setPassword(userDTO.getPassword());

        UserEntity savedEntity = springUserRepository.save(userMapper.userToUserEntity(updateUser));

        return userMapper.userEntityToUserResponseDTO(savedEntity);
    }

    @Override
    public UserResponseDTO findById(Long id, UserDetails userDetails) throws NotFoundException {

        user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND));

        securityConfig.checkAccess(id, userDetails);

        return userMapper.userToUserResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> findAll() {

        return userRepository.findAll().stream()
                .map(userMapper::userToUserResponseDTO).toList();
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
                    .orElseThrow(() -> new NotFoundException(NOT_FOUND));
        } else if (email != null) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException(NOT_FOUND));
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
    public Map<String, Object> deletePermissions(
            Long userId,
            String email,
            boolean allPermissions,
            Set<Permission> permissions
    ) {

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(NOT_FOUND));

        } else if (email != null) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException(NOT_FOUND));

        } else {
            throw new BadRequestException("UserId or email must be provided");
        }

        if (allPermissions) {
            user.setPermissions(null);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("removedAll", true);
            return response;
        }

        return deleteSpecificPermissions(permissions, user);
    }

    @Transactional
    public List<Permission> showPermissions(Long userId, UserDetails userDetails) {

        user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(NOT_FOUND));

        securityConfig.checkAccess(user.getId(), userDetails);

        return new ArrayList<>(user.getPermissions());
    }

    @Override
    public List<UserWithPermissionsDTO> showAllUsersWithPermissions(UserDetails userDetails) {

        securityConfig.checkAdminAccess(userDetails);

        List<User> users = userRepository.findAll();

        return userMapper.userToUserWithPermissionsDTOs(users);
    }

    public Map<String, Object> deleteSpecificPermissions(
            Set<Permission> permissions,
            User user
    ) {

        if (permissions == null || permissions.isEmpty()) {
            throw new BadRequestException("The permission list cannot be empty or null.");
        }

        Set<Permission> permissionsUser = new HashSet<>(user.getPermissions());

        if (permissionsUser.isEmpty()) {
            throw new UserDontHavePermissionsException("The user does not have permissions");
        }

        Set<Permission> notFoundPermissions = new HashSet<>();
        Set<Permission> removedPermissions = new HashSet<>();

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

        return response;
    }
}
