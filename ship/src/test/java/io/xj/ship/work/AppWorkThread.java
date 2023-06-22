// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.work;

public class AppWorkThread extends Thread {

  private final ShipWork work;

  public AppWorkThread(ShipWork work) {
    this.work = work;
  }

  public void run() {
    work.doWork();
  }
}
