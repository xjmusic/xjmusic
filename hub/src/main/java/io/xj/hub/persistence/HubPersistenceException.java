// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import io.xj.lib.util.StringUtils;

public class HubPersistenceException extends Exception {

  public HubPersistenceException(String msg) {
    super(msg);
  }

  public HubPersistenceException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), StringUtils.formatStackTrace(e)));
  }

  public HubPersistenceException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), StringUtils.formatStackTrace(targetException)));
  }
}
