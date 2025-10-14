package com.portafolio.gestor_tareas.exception.domain;

public class InvalidTaskCompleteException extends RuntimeException {

    private static final String DESCRIPTION = "Invalid task completion status";

        public InvalidTaskCompleteException(String detail) {
            super(DESCRIPTION + ". " + detail);
        }
}
