// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.ship;

import io.outright.xj.core.application.Application;
import io.outright.xj.core.application.ApplicationImpl;

import java.io.IOException;

/**
 * Main class.
 *
 */
public class Main {
  private static Application app;

  /**
   * Main method.
   * @param args arguments
   * @throws IOException if execution fails
   */
  public static void main(String[] args) throws IOException {
    int defaultPort = 8044;

    // Application
    app = new ApplicationImpl(
      new String[]{"io.outright.xj.ship"},
      defaultPort
    );

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

    // Start
    app.Start();
  }

  private static void shutdown() {
    app.Stop();
  }

}

