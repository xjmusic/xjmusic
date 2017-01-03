// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app.exception;

public class AccessException extends Exception {

  public AccessException(String msg) {
    super(msg);
  }

  public AccessException(DatabaseException e) {
    super("Database exception: " + e.getMessage());
  }

  public AccessException(Exception e) {
    super(e);
  }
}
