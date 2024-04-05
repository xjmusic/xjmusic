// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.util;

public class ValueException extends Exception {

  public ValueException(String msg) {
    super(msg);
  }

  public ValueException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), StringUtils.formatStackTrace(e)));
  }

  public ValueException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), StringUtils.formatStackTrace(targetException)));
  }
}
