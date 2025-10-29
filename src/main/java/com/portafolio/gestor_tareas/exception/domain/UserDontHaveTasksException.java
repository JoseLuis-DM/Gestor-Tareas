package com.portafolio.gestor_tareas.exception.domain;

public class UserDontHaveTasksException extends RuntimeException {

  private static final String DESCRIPTION = "User does not have any tasks";

  public UserDontHaveTasksException(String detail) {
    super(DESCRIPTION + ". " + detail);
  }
}
