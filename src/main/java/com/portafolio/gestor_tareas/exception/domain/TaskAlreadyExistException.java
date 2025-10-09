package com.portafolio.gestor_tareas.exception.domain;

public class TaskAlreadyExistException extends RuntimeException {

    private static final String DESCRIPTION = "Task already exists";

    public TaskAlreadyExistException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
