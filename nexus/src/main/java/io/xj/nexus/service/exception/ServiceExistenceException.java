// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.service.exception;

public class ServiceExistenceException extends Exception {

  public ServiceExistenceException(String msg) {
    super(msg);
  }

  public ServiceExistenceException(Class<?> type, String name) {
    super(message(type, name));
  }

  public ServiceExistenceException(Class<?> type, String id, String detail) {
    super(String.format("%s %s", message(type, id), detail));
  }

  private static String message(Class<?> type, String identifier) {
    return String.format("%s[%s] does not exist!", type.getSimpleName(), identifier);
  }
}
