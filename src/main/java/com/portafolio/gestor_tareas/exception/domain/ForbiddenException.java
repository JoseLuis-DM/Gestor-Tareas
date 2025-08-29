package com.portafolio.gestor_tareas.exception.domain;

public class ForbiddenException extends RuntimeException {

    private static final String DESCRIPTION = "Forbidden exception";

    public ForbiddenException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
