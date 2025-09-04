package com.portafolio.gestor_tareas.exception.domain;

public class UserAlreadyExistsException extends RuntimeException {

    private static final String DESCRIPTION = "User already exists";

    public UserAlreadyExistsException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
