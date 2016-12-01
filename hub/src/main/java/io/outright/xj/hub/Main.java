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
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

      // Application
      app = new ApplicationImpl(
        new String[]{"io.outright.xj.hub"}
      );

      // Shutdown Hook
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        app.Stop();
      }));

      // Start
      app.Start();
    }

}

