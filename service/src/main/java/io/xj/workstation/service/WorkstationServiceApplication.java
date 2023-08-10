// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.workstation.service;

import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.InputMode;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.hub_client.HubTopology;
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

import java.util.Locale;

@SpringBootApplication
@ComponentScan(
  basePackages = {
    "io.xj.lib",
    "io.xj.hub",
    "io.xj.nexus",
  })
public class WorkstationServiceApplication {
  final Logger LOG = LoggerFactory.getLogger(WorkstationServiceApplication.class);
  final EntityFactory entityFactory;
  final WorkFactory workFactory;
  final ApplicationContext context;
  private final InputMode inputMode;
  private final String inputTemplateKey;
  private final OutputFileMode outputFileMode;
  private final OutputMode outputMode;
  private final String outputPathPrefix;
  private final int outputSeconds;

  @Autowired
  public WorkstationServiceApplication(
    ApplicationContext context,
    EntityFactory entityFactory,
    WorkFactory workFactory,
    @Value("${input.mode}") String inputMode,
    @Value("${input.template.key}") String inputTemplateKey,
    @Value("${output.file.mode}") String outputFileMode,
    @Value("${output.mode}") String outputMode,
    @Value("${output.path.prefix}") String outputPathPrefix,
    @Value("${output.seconds}") int outputSeconds
  ) {
    this.entityFactory = entityFactory;
    this.workFactory = workFactory;
    this.context = context;
    this.inputMode = InputMode.valueOf(inputMode.toUpperCase(Locale.ROOT));
    this.inputTemplateKey = inputTemplateKey;
    this.outputFileMode = OutputFileMode.valueOf(outputFileMode.toUpperCase(Locale.ROOT));
    this.outputMode = OutputMode.valueOf(outputMode.toUpperCase(Locale.ROOT));
    this.outputPathPrefix = outputPathPrefix;
    this.outputSeconds = outputSeconds;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void start() {
    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    workFactory.start(
      inputMode,
      inputTemplateKey,
      outputFileMode,
      outputMode,
      outputPathPrefix,
      outputSeconds, this::shutdown
    );
  }

  void shutdown() {
    LOG.info("will shutdown");
    Thread shutdown = new Thread(() -> {
      ((ConfigurableApplicationContext) context).close();
      LOG.info("did finish work and shutdown OK");
    });
    shutdown.setDaemon(false);
    shutdown.start();
  }

  public static void main(String[] args) {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(WorkstationServiceApplication.class);
    builder.run(args);
  }
}
