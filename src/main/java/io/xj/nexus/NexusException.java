// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import io.xj.lib.util.Text;

public class NexusException extends Exception {

  public NexusException(String msg) {
    super(msg);
  }

  public NexusException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public NexusException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }

  public NexusException() {
    super();
  }
}
