package com.portafolio.gestor_tareas.task.infrastructure.repository;

import com.portafolio.gestor_tareas.task.infrastructure.entity.TaskEntity;
import com.portafolio.gestor_tareas.users.domain.User;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringTaskRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findByUserIdAndTitleIgnoreCase(Long userId, String title);

    List<TaskEntity> findByUser(UserEntity user);
}
