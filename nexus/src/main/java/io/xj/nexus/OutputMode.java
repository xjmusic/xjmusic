// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus;

public enum OutputMode {
  HLS,
  PLAYBACK,
  FILE;

  public Boolean isLocal() {
    return this == PLAYBACK || this == FILE;
  }

  public Boolean isSync() {
    return this == HLS || this == PLAYBACK;
  }

  public Boolean isAsync() {
    return !this.isSync();
  }
}
