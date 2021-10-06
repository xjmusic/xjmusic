// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.lib.util.Text;

public class ManagerFatalException extends Exception {

  public ManagerFatalException(String msg) {
    super(msg);
  }

  public ManagerFatalException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public ManagerFatalException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }
}
