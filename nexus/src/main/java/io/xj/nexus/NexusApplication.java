// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import ch.qos.logback.classic.LoggerContext;
import io.xj.hub.HubTopology;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.StringUtils;
import io.xj.nexus.work.WorkFactory;
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

@SpringBootApplication
@ComponentScan(
  basePackages = {
    "io.xj.lib",
    "io.xj.hub.client",
    "io.xj.hub.service",
    "io.xj.nexus",
  })
public class NexusApplication {
  final Logger LOG = LoggerFactory.getLogger(NexusApplication.class);
  final EntityFactory entityFactory;
  final AppConfiguration config;
  final WorkFactory workFactory;
  final ApplicationContext context;

  @Value("${hostname}")
  String hostname;
  @Value("${environment}")
  String environment;

  @Autowired
  public NexusApplication(
    AppConfiguration config,
    ApplicationContext context,
    EntityFactory entityFactory,
    WorkFactory workFactory
  ) {
    this.entityFactory = entityFactory;
    this.config = config;
    this.workFactory = workFactory;
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

    workFactory.start(this::shutdown);
  }

  void shutdown() {
    LOG.info("{} will shutdown", StringUtils.toProper(config.getName()));
    Thread shutdown = new Thread(() -> {
      ((ConfigurableApplicationContext) context).close();
      LOG.info("{} did finish work and shutdown OK", StringUtils.toProper(config.getName()));
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
