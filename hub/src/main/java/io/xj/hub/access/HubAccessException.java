// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import io.xj.lib.util.Text;

public class HubAccessException extends Exception {

  public HubAccessException(String msg) {
    super(msg);
  }

  public HubAccessException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public HubAccessException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }
}
