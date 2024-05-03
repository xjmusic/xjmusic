// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.persistence;

public class ManagerExistenceException extends Exception {

  public ManagerExistenceException(String msg) {
    super(msg);
  }

  public ManagerExistenceException(Class<?> type, String name) {
    super(message(type, name));
  }

  public ManagerExistenceException(Class<?> type, String id, String detail) {
    super(String.format("%s %s", message(type, id), detail));
  }

  static String message(Class<?> type, String identifier) {
    return String.format("%s[%s] does not exist!", type.getSimpleName(), identifier);
  }
}
