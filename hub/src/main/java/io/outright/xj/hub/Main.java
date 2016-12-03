package io.outright.xj.hub;

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
      int defaultPort = 8042;

      // Application
      app = new ApplicationImpl(
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

