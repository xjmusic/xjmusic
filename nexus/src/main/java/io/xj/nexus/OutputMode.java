// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus;

public enum OutputMode {
  PLAYBACK,
  FILE;

  public Boolean isLocal() {
    return this == PLAYBACK || this == FILE;
  }

  public Boolean isSync() {
    return this == PLAYBACK;
  }

  public Boolean isAsync() {
    return !this.isSync();
  }
}
