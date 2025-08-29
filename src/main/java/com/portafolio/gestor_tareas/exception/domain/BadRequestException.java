package com.portafolio.gestor_tareas.exception.domain;

public class BadRequestException extends RuntimeException {

    private static final String DESCRIPTION = "Bad request exception";

    public BadRequestException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
