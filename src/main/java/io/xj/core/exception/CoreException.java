// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.exception;

import io.xj.core.util.Text;

public class CoreException extends Exception {

  public CoreException(String msg) {
    super(msg);
  }

  public CoreException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public CoreException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }
}
