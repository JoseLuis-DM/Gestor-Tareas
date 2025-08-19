package com.portafolio.gestor_tareas.users.domain;

import java.util.Optional;
import java.util.List;

public interface UserService {

    User register(User user);

    User update(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void delete(Long id);
}
