// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.service;

import io.xj.lib.util.StringUtils;

public class ServiceException extends Exception {

  public ServiceException(String msg) {
    super(msg);
  }

  public ServiceException(String msg, Exception e) {
    super(String.format("%s %s", msg, e.getMessage()));
  }

  public ServiceException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), StringUtils.formatStackTrace(targetException)));
  }
}
