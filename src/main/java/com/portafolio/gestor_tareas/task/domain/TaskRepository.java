package com.portafolio.gestor_tareas.task.domain;

import com.portafolio.gestor_tareas.users.domain.User;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    void deleteById(Long id);

    Optional<Task> findByUserIdAndTitleIgnoreCase(Long userId, String title);

    List<Task> findByUser(User user);
}
