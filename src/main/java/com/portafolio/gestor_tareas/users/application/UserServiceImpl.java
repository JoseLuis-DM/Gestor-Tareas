package com.portafolio.gestor_tareas.users.application;

import com.portafolio.gestor_tareas.exception.domain.BadRequestException;
import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
import com.portafolio.gestor_tareas.exception.domain.UserAlreadyExistsException;
import com.portafolio.gestor_tareas.users.domain.Permission;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.domain.UserService;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import com.portafolio.gestor_tareas.users.infrastructure.repository.SpringUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User register(User user) {

        if (userRepository.existsEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("The email is already registered");
        }

        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        User updateUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!updateUser.getEmail().equals(user.getEmail()) &&
                userRepository.existsEmail(user.getEmail())) {
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

        User user = userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("The user does not exist"));
        userRepository.deleteById(id);
    }

    @Transactional
    public void addPermissions(Long userId, String email, Set<Permission> permissions) {
        User user;

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
        } else if (email != null) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found"));
        } else {
            throw new BadRequestException("UserId or email must be provided");
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new BadRequestException("Permissions must be provided");
        }

        user.getPermissions().addAll(permissions);

        userRepository.save(user);
    }
}
