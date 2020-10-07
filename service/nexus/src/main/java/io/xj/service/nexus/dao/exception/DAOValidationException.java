// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.dao.exception;

public class DAOValidationException extends Exception {

  public DAOValidationException(String msg) {
    super(msg);
  }

  public DAOValidationException(String msg, Exception e) {
    super(msg, e);
  }

  public DAOValidationException(Exception e) {
    super(e.getMessage(), e);
  }
}
