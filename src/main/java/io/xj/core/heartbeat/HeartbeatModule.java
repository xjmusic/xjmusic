// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.heartbeat;

import com.google.inject.AbstractModule;

public class HeartbeatModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Heartbeat.class).to(HeartbeatImpl.class);
  }
}
