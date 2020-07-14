// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.persistence;

import io.xj.lib.util.Text;

public class NexusEntityStoreException extends Exception {

  public NexusEntityStoreException(String msg) {
    super(msg);
  }

  public NexusEntityStoreException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public NexusEntityStoreException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }
}
