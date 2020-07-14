// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.dao.exception;

import java.util.UUID;

public class DAOExistenceException extends Exception {

  public DAOExistenceException(String msg) {
    super(msg);
  }

  public DAOExistenceException(Class<?> type, UUID id) {
    super(message(type, id.toString()));
  }

  public DAOExistenceException(Class<?> type, String name) {
    super(message(type, name));
  }

  public DAOExistenceException(Class<?> type, UUID id, String detail) {
    super(String.format("%s %s", message(type, id.toString()), detail));
  }

  private static String message(Class<?> type, String identifier) {
    return String.format("%s[%s] does not exist!", type.getSimpleName(), identifier);
  }
}
