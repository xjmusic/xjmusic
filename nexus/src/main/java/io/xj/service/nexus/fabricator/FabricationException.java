// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.fabricator;

import io.xj.lib.util.Text;

public class FabricationException extends Exception {

  public FabricationException(String msg) {
    super(msg);
  }

  public FabricationException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public FabricationException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }
}
