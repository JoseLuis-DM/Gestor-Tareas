package com.portafolio.gestor_tareas.users.infrastructure.repository;

import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.domain.UserRepository;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import com.portafolio.gestor_tareas.users.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MySqlUserRepository implements UserRepository {

    private final SpringUserRepository springUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User userEntity) {
        UserEntity entity = userMapper.userToUserEntity(userEntity);
        UserEntity saved = springUserRepository.save(entity);
        return userMapper.userEntityToUser(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springUserRepository.findById(id)
                .map(userMapper::userEntityToUser);
    }

    @Override
    public List<User> findAll() {
        return springUserRepository.findAll()
                .stream().map(userMapper::userEntityToUser).toList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springUserRepository.findByEmail(email)
                .map(userMapper::userEntityToUser);
    }

    @Override
    public Boolean existsEmail(String email) {
        return springUserRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(Long id) {
        springUserRepository.deleteById(id);
    }
}
