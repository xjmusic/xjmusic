// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.mixer;

public class FormatException extends Exception {

  public FormatException(String msg) {
    super(msg);
  }

  public FormatException(Exception e) {
    super(e);
  }

  public FormatException(String msg, Exception e) {
    super(msg, e);
  }
}
