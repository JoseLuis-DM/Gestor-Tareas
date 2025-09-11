package com.portafolio.gestor_tareas.task.infrastructure.entity;

import com.portafolio.gestor_tareas.audit.Auditable;
import com.portafolio.gestor_tareas.users.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_tasks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "title"})
)
public class TaskEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // (Equals y hashcode) evita problemas con relaciones bidireccionales al solo usar el id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskEntity)) return false;
        TaskEntity task = (TaskEntity) o;
        return id != null && id.equals(task.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
