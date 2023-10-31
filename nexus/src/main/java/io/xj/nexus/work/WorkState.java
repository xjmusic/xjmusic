// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;


public enum WorkState {
  Standby,
  Starting,
  LoadingContent,
  LoadedContent,
  PreparingAudio,
  PreparedAudio,
  Initializing,
  Active,
  Done,
  Cancelled,
  Failed,
}
