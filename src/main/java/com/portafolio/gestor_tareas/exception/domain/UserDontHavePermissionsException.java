package com.portafolio.gestor_tareas.exception.domain;

public class UserDontHavePermissionsException extends RuntimeException {

    private static final String DESCRIPTION = "User does not have any permissions";

    public UserDontHavePermissionsException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }
}
