// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craft;

import io.outright.xj.core.application.Application;
import io.outright.xj.core.application.ApplicationModule;
import io.outright.xj.core.util.DefaultProperty;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;

/**
 * Main class.
 *
 */
public class Main {
  private static Application app;
  private static final Injector injector = Guice.createInjector(new ApplicationModule());

  /**
   * Main method.
   * @param args arguments
   * @throws IOException if execution fails
   */
  public static void main(String[] args) throws IOException {
    // Default port
    DefaultProperty.setIfNotAlready("app.port","8042");

    // Application
    app = injector.getInstance(Application.class);
    app.Configure("io.outright.xj.craft");

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

    // Start
    app.Start();
  }

  private static void shutdown() {
    app.Stop();
  }

}

