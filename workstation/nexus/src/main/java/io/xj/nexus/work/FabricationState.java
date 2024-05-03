// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;


public enum FabricationState {
  Standby,
  Starting,
  PreparingAudio,
  PreparedAudio,
  Initializing,
  Active,
  Done,
  Cancelled,
  Failed,
}
