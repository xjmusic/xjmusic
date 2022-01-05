// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

public class AppWorkThread extends Thread {
  private final NexusWork work;

  public AppWorkThread(NexusWork work) {
    this.work = work;
  }

  public void run() {
    work.start();
  }
}
