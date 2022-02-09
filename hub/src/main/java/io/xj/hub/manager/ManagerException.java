// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

public class ManagerException extends Exception {

  public ManagerException(String msg) {
    super(msg);
  }

  public ManagerException(String msg, Exception e) {
    super(msg, e);
  }

  public ManagerException(Throwable e) {
    super(e.getMessage(), e);
    setStackTrace(e.getStackTrace());
  }
}
