// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.exception;

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
