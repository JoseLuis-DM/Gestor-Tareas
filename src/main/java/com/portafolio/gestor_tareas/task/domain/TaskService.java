package com.portafolio.gestor_tareas.task.domain;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    Task save(Task task, Long userId);

    Task update(Task task, Long userId, UserDetails userDetails);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    void delete(Long id, UserDetails userDetails);
}
