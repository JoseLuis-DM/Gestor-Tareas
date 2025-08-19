package com.portafolio.gestor_tareas.users.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User userEntity);

    Optional<User> findById(Long id);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    Boolean existsEmail(String email);

    void deleteById(Long id);

}
