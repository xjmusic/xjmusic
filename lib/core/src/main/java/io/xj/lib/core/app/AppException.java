// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.app;

public class AppException extends Exception {
  public AppException(String message) {
    super(message);
  }

  public AppException(String message, Throwable t) {
    super(message, t);
  }
}
