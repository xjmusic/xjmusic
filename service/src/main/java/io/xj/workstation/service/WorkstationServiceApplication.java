// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.workstation.service;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.hub.enums.UserRoleType;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.InputMode;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubContentProvider;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.work.WorkConfiguration;
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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

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
  private final HubClient hubClient;
  final InputMode inputMode;
  final String inputTemplateKey;
  final OutputFileMode outputFileMode;
  final OutputMode outputMode;
  final String outputPathPrefix;
  final int outputSeconds;
  private final String ingestToken;
  private final String audioBaseUrl;
  private final String labBaseUrl;
  private final String shipBaseUrl;
  private final String streamBaseUrl;

  @Autowired
  public WorkstationServiceApplication(
    ApplicationContext context,
    EntityFactory entityFactory,
    WorkFactory workFactory,
    HubClient hubClient,
    @Value("${input.mode}") String inputMode,
    @Value("${input.template.key}") String inputTemplateKey,
    @Value("${output.file.mode}") String outputFileMode,
    @Value("${output.mode}") String outputMode,
    @Value("${output.path.prefix}") String outputPathPrefix,
    @Value("${output.seconds}") int outputSeconds,
    @Value("${ingest.token}") String ingestToken,
    @Value("${audio.base.url}") String audioBaseUrl,
    @Value("${lab.base.url}") String labBaseUrl,
    @Value("${ship.base.url}") String shipBaseUrl,
    @Value("${stream.base.url}") String streamBaseUrl
  ) {
    this.entityFactory = entityFactory;
    this.workFactory = workFactory;
    this.context = context;
    this.hubClient = hubClient;
    this.inputMode = InputMode.valueOf(inputMode.toUpperCase(Locale.ROOT));
    this.inputTemplateKey = inputTemplateKey;
    this.outputFileMode = OutputFileMode.valueOf(outputFileMode.toUpperCase(Locale.ROOT));
    this.outputMode = OutputMode.valueOf(outputMode.toUpperCase(Locale.ROOT));
    this.outputPathPrefix = outputPathPrefix;
    this.outputSeconds = outputSeconds;
    this.ingestToken = ingestToken;
    this.audioBaseUrl = audioBaseUrl;
    this.labBaseUrl = labBaseUrl;
    this.shipBaseUrl = shipBaseUrl;
    this.streamBaseUrl = streamBaseUrl;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void start() {
    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    var workConfig = new WorkConfiguration()
      .setInputMode(inputMode)
      .setInputTemplateKey(inputTemplateKey)
      .setOutputFileMode(outputFileMode)
      .setOutputMode(outputMode)
      .setOutputPathPrefix(outputPathPrefix)
      .setOutputSeconds(outputSeconds);

    var hubConfig = new HubConfiguration()
      .setApiBaseUrl(labBaseUrl)
      .setAudioBaseUrl(audioBaseUrl)
      .setBaseUrl(labBaseUrl)
      .setPlayerBaseUrl(streamBaseUrl)
      .setShipBaseUrl(shipBaseUrl)
      .setStreamBaseUrl(streamBaseUrl);

    var hubAccess = new HubClientAccess()
      .setRoleTypes(List.of(UserRoleType.Internal))
      .setToken(ingestToken);

    Callable<HubContent> hubContentProvider = new HubContentProvider(hubClient, hubConfig, hubAccess, inputMode, workConfig.getInputTemplateKey());
    workFactory.start(workConfig, hubConfig, hubContentProvider, sourceMaterialReadyCallback, this::updateProgress, this::shutdown);
  }

  private void updateProgress(Double aDouble) {
    // Future: telemetry
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
