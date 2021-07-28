// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.NexusApp;

public class AppWorkThread extends Thread {

  private final NexusApp app;

  public AppWorkThread(NexusApp app) {
    this.app = app;
  }

  public void run() {
    app.getWork().work();
  }
}
