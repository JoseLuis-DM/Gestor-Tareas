package com.portafolio.gestor_tareas.task.infrastructure.repository;

import com.portafolio.gestor_tareas.task.domain.Task;
import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringTaskRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findByUserIdAndTitleIgnoreCase(Long userId, String title);
}
