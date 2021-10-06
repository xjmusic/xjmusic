// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import com.google.inject.Inject;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Base application for Ocean Backend services.
 * Created by Charney Kaye on 2019/11/29
 * <p>
 * USAGE
 * <p>
 * + Create a Guice injector that will be used throughout the entire application, by means of:
 * - Creating an application with new App(pathToConfigFile, resourcePackages, injector) <-- pass in Guice injector
 * - Making that injector available to Jersey2-based resources for their injection
 * - Ensuring all classes within the application are injected via their constructors (NOT creating another injector)
 * - ensuring all classes rely on factory and provider modules (NOT creating another injector)
 * <p>
 * + Accept one runtime argument, pointing to the location of a TypeSafe config
 * - ingest that configuration and make it available throughout the application
 * <p>
 * + Perform relational database migration
 * - do not start the app if migrations are out of sync
 * <p>
 * + Configure Jersey server resources
 * <p>
 * + Call application start()
 * - Add shutdown hook that calls application stop()
 */
public class App {
  private static final Logger log = LoggerFactory.getLogger(App.class);
  private final ResourceConfig resourceConfig;
  private final String name;
  private final String hostname;
  private final int port;
  private Server resourceServer;
  private boolean started = false;

  @Inject
  public App(
    String name,
    Environment env
  ) {
    this.name = name;

    hostname = computeInetHostname(env.getHostname());
    port = env.getAppPort();

    resourceConfig = new ResourceConfig();
  }

  /**
   * Get the resource configuration, only before the app has been started—
   * e.g. in order to register additional filters before starting the app.
   *
   * @return resource configuration
   */
  public ResourceConfig getResourceConfig() {
    if (started) log.warn("Should not modify resource configuration after app has started!");
    return resourceConfig;
  }

  /**
   * Get name of application, probably simple name of main app class, e.g. Hub
   *
   * @return name of application
   */
  public String getName() {
    return name;
  }

  /**
   * Get inet hostname value if available, else fallback value
   *
   * @param fallbackValue to use if inet hostname is not available
   * @return inet hostname value if available, else fallback value
   */
  protected String computeInetHostname(String fallbackValue) {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ignored) {
      return fallbackValue;
    }
  }

  /**
   * Create a Jetty server from the configuration
   *
   * @return Jetty server
   */
  private Server startResourceServer() throws AppException {
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    var jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));

    Server server = new Server(getPort());
    server.setHandler(context);
    context.addServlet(jerseyServlet, "/*");

    server.setRequestLog(new AppRequestLogFactory(log).create());

    try {
      server.start();
    } catch (Exception e) {
      throw new AppException("Failed to start resource server", e);
    }
    return server;
  }

  /**
   * Starts Jetty HTTP server
   * exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    log.debug("{} will start", name);
    started = true;

    // Jetty
    resourceServer = startResourceServer();

    log.info("{} did start OK, listening at {}", name, resourceServer.getURI());
  }

  /**
   * stop App Server
   */
  public void finish() {
    log.debug("{} will stop", name);

    if (Objects.nonNull(resourceServer))
      try {
        resourceServer.stop();
        resourceServer.destroy();
      } catch (Exception e) {
        log.error("{} failed to stop resource server", name, e);
      }

    log.info("{} did stop", name);
  }

  public int getPort() {
    return port;
  }

  public String getHostname() {
    return hostname;
  }
}
