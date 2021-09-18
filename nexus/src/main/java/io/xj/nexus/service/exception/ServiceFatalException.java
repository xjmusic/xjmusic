// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.service.exception;

import io.xj.lib.util.Text;

public class ServiceFatalException extends Exception {

  public ServiceFatalException(String msg) {
    super(msg);
  }

  public ServiceFatalException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public ServiceFatalException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }
}
