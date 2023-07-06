// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import ch.qos.logback.classic.LoggerContext;
import io.xj.hub.HubTopology;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Text;
import io.xj.nexus.work.CraftWork;
import io.xj.nexus.work.DubWork;
import io.xj.nexus.work.ShipWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

@SpringBootApplication
@ComponentScan(
  basePackages = {
    "io.xj.lib",
    "io.xj.hub.client",
    "io.xj.hub.service",
    "io.xj.nexus",
  })
public class NexusApplication {
  private static final String APPLICATION_WINDOW_TITLE = "XJ music";
  final Logger LOG = LoggerFactory.getLogger(NexusApplication.class);
  private final EntityFactory entityFactory;
  private final AppConfiguration config;
  private final CraftWork craftWork;
  private final DubWork dubWork;
  private final ShipWork shipWork;
  private final ApplicationContext context;

  @Value("${server.hostname}")
  String hostname;
  @Value("${environment}")
  String environment;

  @Autowired
  public NexusApplication(
    AppConfiguration config,
    ApplicationContext context,
    EntityFactory entityFactory,
    CraftWork craftWork,
    DubWork dubWork,
    ShipWork shipWork
  ) {
    this.entityFactory = entityFactory;
    this.config = config;
    this.craftWork = craftWork;
    this.dubWork = dubWork;
    this.shipWork = shipWork;
    this.context = context;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void start() throws NexusException {
    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "nexus");
    lc.putProperty("host", hostname);
    lc.putProperty("env", environment);

    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Confirm workers are instantiated
    if (Objects.isNull(craftWork)) throw new NexusException("Failed to instantiate Craft work");
    if (Objects.isNull(dubWork)) throw new NexusException("Failed to instantiate Dub work");
    if (Objects.isNull(shipWork)) throw new NexusException("Failed to instantiate Ship work");

    buildGUI();

    LOG.info("{} will start", Text.toProper(config.getName()));
    try {
      // Run work on separate threads.
      Thread craftThread = new Thread(craftWork::start);
      Thread dubThread = new Thread(dubWork::start);
      Thread shipThread = new Thread(shipWork::start);
      craftThread.start();
      dubThread.start();
      shipThread.start();

      // This blocks until a graceful exit on interrupt signal or Dub work complete
      dubThread.join();
      craftThread.join();
      shipThread.join();

      // Shutdown the Spring Boot application
      shutdown();

    } catch (InterruptedException e) {
      LOG.info("{} was interrupted", config.getName());
    }
  }

  private void buildGUI() {
    JFrame frame = new JFrame(APPLICATION_WINDOW_TITLE);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(640, 480);
    JPanel panel = new JPanel(new BorderLayout());
    frame.setContentPane(panel);
    frame.setVisible(true);
  }

  private void shutdown() {
    LOG.info("{} will shutdown", Text.toProper(config.getName()));
    Thread shutdown = new Thread(() -> {
      ((ConfigurableApplicationContext) context).close();
      LOG.info("{} did finish work and shutdown OK", Text.toProper(config.getName()));
    });
    shutdown.setDaemon(false);
    shutdown.start();
  }

  public static void main(String[] args) {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(NexusApplication.class);
    builder.headless(false);
    builder.run(args);
  }
}
