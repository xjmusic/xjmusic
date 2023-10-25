// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;


public enum WorkState { // TODO deprecate this once the entire work state is managed from the fabrication service
  Standby,
  Starting,
  LoadingContent,
  LoadedContent,
  LoadingAudio,
  LoadedAudio,
  Initializing,
  Active,
  Done,
  Failed,
}
