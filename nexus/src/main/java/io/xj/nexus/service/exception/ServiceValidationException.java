// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.service.exception;

public class ServiceValidationException extends Exception {

  public ServiceValidationException(String msg) {
    super(msg);
  }

  public ServiceValidationException(String msg, Exception e) {
    super(msg, e);
  }

  public ServiceValidationException(Exception e) {
    super(e.getMessage());
  }
}
