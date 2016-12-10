// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.hub;

import io.outright.xj.core.application.Application;
import io.outright.xj.core.application.ApplicationImpl;
import io.outright.xj.core.application.server.*;

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
      int defaultPort = 8042;
      HttpServerFactory httpServerFactory = new HttpServerFactoryImpl();
      ResourceConfigFactory resourceConfigFactory = new ResourceConfigFactoryImpl();
      LogFilterFactory logFilterFactory = new LogFilterFactoryImpl();

      // Application
      app = new ApplicationImpl(
        httpServerFactory,
        resourceConfigFactory,
        logFilterFactory,
        new String[]{"io.outright.xj.hub"},
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

