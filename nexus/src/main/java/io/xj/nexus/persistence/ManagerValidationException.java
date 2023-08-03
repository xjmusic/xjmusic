// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.persistence;

public class ManagerValidationException extends Exception {

  public ManagerValidationException(String msg) {
    super(msg);
  }

  public ManagerValidationException(String msg, Exception e) {
    super(msg, e);
  }

  public ManagerValidationException(Exception e) {
    super(e.getMessage());
  }
}
