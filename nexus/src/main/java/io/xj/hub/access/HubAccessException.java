// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.hub.access;

import io.xj.lib.util.StringUtils;

public class HubAccessException extends Exception {

  public HubAccessException(String msg) {
    super(msg);
  }

  public HubAccessException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), StringUtils.formatStackTrace(e)));
  }

  public HubAccessException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), StringUtils.formatStackTrace(targetException)));
  }
}
