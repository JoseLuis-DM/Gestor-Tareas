package com.portafolio.gestor_tareas.users.application;

import com.portafolio.gestor_tareas.exception.domain.NotFoundException;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SpringUserRepository springUserRepository;
    private final UserMapper userMapper;

    @Override
    public User register(User user) {

        if (userRepository.existsEmail(user.getEmail())) {
            throw new RuntimeException("The email is already registered");
        }

        return userRepository.save(user);
    }

    @Override
    public User update(User user) {

        User updateUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!updateUser.getEmail().equals(user.getEmail()) &&
                userRepository.existsEmail(user.getEmail())) {
            throw new RuntimeException("The email is already registered by another user");
        }

        BeanUtils.copyProperties(user, updateUser, "role", "id", "password");

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
        userRepository.deleteById(id);
    }

    @Transactional
    public void addPermissions(Long userId, String email, Set<Permission> permissions) {
        UserEntity user;

        if (userId != null) {
            user = springUserRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else if (email != null) {
            user = springUserRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            throw new IllegalArgumentException("UserId or email must be provided");
        }

        if (user.getPermissions() == null) {
            user.setPermissions(new HashSet<>());
        }

        user.getPermissions().addAll(permissions);

        springUserRepository.save(user);
    }
}
