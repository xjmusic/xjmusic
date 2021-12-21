// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

public class DAOException extends Exception {

  public DAOException(String msg) {
    super(msg);
  }

  public DAOException(String msg, Exception e) {
    super(msg, e);
  }

  public DAOException(Throwable e) {
    super(e.getMessage(), e);
    setStackTrace(e.getStackTrace());
  }
}
