// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.work;

import io.xj.ship.ShipApp;

public class AppWorkThread extends Thread {

  private final ShipApp app;

  public AppWorkThread(ShipApp app) {
    this.app = app;
  }

  public void run() {
    app.getWork().work();
  }
}
