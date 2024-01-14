// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.workstation.service;

import io.xj.hub.HubConfiguration;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectManagerImpl;
import io.xj.nexus.work.FabricationConfiguration;
import io.xj.nexus.work.FabricationManager;
import io.xj.nexus.work.FabricationManagerImpl;
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
    "io.xj.hub",
    "io.xj.nexus",
  })
public class WorkstationServiceApplication {
  final Logger LOG = LoggerFactory.getLogger(WorkstationServiceApplication.class);
  final FabricationManager fabricationManager;
  final ApplicationContext context;
  final String inputTemplateKey;
  private final String ingestToken;
  private final String audioBaseUrl;
  private final String labBaseUrl;
  private final String shipBaseUrl;
  private final String streamBaseUrl;


  @Autowired
  public WorkstationServiceApplication(
    ApplicationContext context,
    @Value("${input.template.key}") String inputTemplateKey,
    @Value("${ingest.token}") String ingestToken,
    @Value("${audio.base.url}") String audioBaseUrl,
    @Value("${lab.base.url}") String labBaseUrl,
    @Value("${ship.base.url}") String shipBaseUrl,
    @Value("${stream.base.url}") String streamBaseUrl
  ) {
    this.context = context;
    this.inputTemplateKey = inputTemplateKey;
    this.ingestToken = ingestToken;
    this.audioBaseUrl = audioBaseUrl;
    this.labBaseUrl = labBaseUrl;
    this.shipBaseUrl = shipBaseUrl;
    this.streamBaseUrl = streamBaseUrl;
    ProjectManager projectManager = ProjectManagerImpl.createInstance();
    this.fabricationManager = FabricationManagerImpl.createInstance(projectManager);
  }

  @EventListener(ApplicationStartedEvent.class)
  public void start() {
    var workConfig = new FabricationConfiguration()
      .setInputTemplate(null); // FUTURE: read template

    var hubConfig = new HubConfiguration()
      .setApiBaseUrl(labBaseUrl)
      .setAudioBaseUrl(audioBaseUrl)
      .setBaseUrl(labBaseUrl)
      .setPlayerBaseUrl(streamBaseUrl)
      .setShipBaseUrl(shipBaseUrl)
      .setStreamBaseUrl(streamBaseUrl);

    var hubAccess = new HubClientAccess()
      .setToken(ingestToken);

    fabricationManager.setAfterFinished(this::shutdown);
    fabricationManager.start(workConfig, hubConfig, hubAccess);
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
