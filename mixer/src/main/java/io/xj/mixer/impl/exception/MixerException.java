// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl.exception;

public class MixerException extends Exception {

  public MixerException(String msg) {
    super(msg);
  }

  public MixerException(Exception e) {
    super(e);
  }

  public MixerException(String msg, Exception e) {
    super(msg, e);
  }
}
