package com.portafolio.gestor_tareas.exception.domain;

public class RefreshTokenNotFoundException extends RuntimeException {

    private static final String DESCRIPTION = "RefreshToken not found exception";

    public RefreshTokenNotFoundException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
