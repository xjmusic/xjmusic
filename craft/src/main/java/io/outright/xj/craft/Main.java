// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craft;

import io.outright.xj.core.app.App;
import io.outright.xj.core.app.CoreModule;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.ConfigException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;

/**
 * Main class.
 *
 */
public class Main {
  private static App app;
  private static final Injector injector = Guice.createInjector(new CoreModule());

  /**
   * Main method.
   * @param args arguments
   * @throws IOException if execution fails
   */
  public static void main(String[] args) throws IOException, ConfigException {
    // Default port
    Config.setDefault("app.port", "8043");

    // App
    app = injector.getInstance(App.class);
    app.configure("io.outright.xj.craft");

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

    // start
    app.start();
  }

  private static void shutdown() {
    app.stop();
  }

}

