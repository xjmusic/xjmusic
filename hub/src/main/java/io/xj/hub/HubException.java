// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import io.xj.lib.util.StringUtils;

public class HubException extends Exception {

  public HubException(String msg) {
    super(msg);
  }

  public HubException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), StringUtils.formatStackTrace(e)));
  }

  public HubException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), StringUtils.formatStackTrace(targetException)));
  }
}
