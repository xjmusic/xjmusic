// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.kubernetes;

import io.xj.lib.util.Text;

public class KubernetesException extends Exception {

  public KubernetesException(String msg) {
    super(msg);
  }

  public KubernetesException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

  public KubernetesException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), Text.formatStackTrace(targetException)));
  }
}
