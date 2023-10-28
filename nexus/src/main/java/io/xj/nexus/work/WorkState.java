// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;


public enum WorkState {
  Standby,
  Starting,
  LoadingContent,
  LoadedContent,
  LoadingAudio,
  LoadedAudio,
  Initializing,
  Active,
  Done,
  Cancelled,
  Failed,
}
