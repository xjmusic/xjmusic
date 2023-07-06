// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

public class AppWorkThread extends Thread {
  private final CraftWork work;

  public AppWorkThread(CraftWork work) {
    this.work = work;
  }

  public void run() {
    work.start();
  }
}
