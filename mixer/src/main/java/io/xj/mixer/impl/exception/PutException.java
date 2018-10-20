// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl.exception;


public class PutException extends Exception {

  public PutException(String msg) {
    super(msg);
  }

  public PutException(Exception e) {
    super(e);
  }

  public PutException(String msg, Exception e) {
    super(msg, e);
  }
}
