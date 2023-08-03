// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus;

public enum OutputMode {
  HLS,
  PLAYBACK,
  FILE;

  public boolean isLocal() {
    return this == PLAYBACK || this == FILE;
  }

  public boolean isSync() {
    return this == HLS || this == PLAYBACK;
  }

  public boolean isAsync() {
    return !this.isSync();
  }
}
