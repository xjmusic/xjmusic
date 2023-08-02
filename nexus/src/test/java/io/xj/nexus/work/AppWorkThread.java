// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

public class AppWorkThread extends Thread {
  final CraftWork work;

  public AppWorkThread(CraftWork work) {
    this.work = work;
  }

  public void run() {
    work.start();
  }
}
