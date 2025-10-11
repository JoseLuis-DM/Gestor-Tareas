package com.portafolio.gestor_tareas.exception.domain;

public class RefreshTokenRevokedException extends RuntimeException {

    private static final String DESCRIPTION = "RefreshToken revoked";

    public RefreshTokenRevokedException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
