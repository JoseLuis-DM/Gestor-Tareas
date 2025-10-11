package com.portafolio.gestor_tareas.exception.domain;

public class RefreshTokenExpiredException extends RuntimeException {

    private static final String DESCRIPTION = "RefreshToken expired";

    public RefreshTokenExpiredException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
