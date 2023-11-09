// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;

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
