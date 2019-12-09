// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl.exception;

public class SourceException extends Exception {

  public SourceException(String msg) {
    super(msg);
  }

  public SourceException(Exception e) {
    super(e);
  }

  public SourceException(String msg, Exception e) {
    super(msg, e);
  }

}
