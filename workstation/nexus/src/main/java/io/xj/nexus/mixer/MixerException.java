// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;

public class MixerException extends Exception {

  public MixerException(String msg) {
    super(msg);
  }

  public MixerException(Exception e) {
    super(e);
  }

  public MixerException(String msg, Exception e) {
    super(msg, e);
    setStackTrace(e.getStackTrace());
  }
}
