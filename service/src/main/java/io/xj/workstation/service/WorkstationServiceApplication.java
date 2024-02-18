// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.workstation.service;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.audio.AudioCache;
import io.xj.nexus.audio.AudioCacheImpl;
import io.xj.nexus.audio.AudioLoader;
import io.xj.nexus.audio.AudioLoaderImpl;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.http.HttpClientProviderImpl;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubClientFactory;
import io.xj.nexus.hub_client.HubClientFactoryImpl;
import io.xj.nexus.mixer.EnvelopeProvider;
import io.xj.nexus.mixer.EnvelopeProviderImpl;
import io.xj.nexus.mixer.MixerFactory;
import io.xj.nexus.mixer.MixerFactoryImpl;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectManagerImpl;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.BroadcastFactoryImpl;
import io.xj.nexus.telemetry.Telemetry;
import io.xj.nexus.telemetry.TelemetryImpl;
import io.xj.nexus.work.FabricationManager;
import io.xj.nexus.work.FabricationManagerImpl;
import io.xj.nexus.work.FabricationSettings;
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
      @Value("${audio.base.url}") String audioBaseUrl,
      @Value("${audio.download.retries}") int audioDownloadRetries,
      @Value("${ingest.token}") String ingestToken,
      @Value("${input.template.key}") String inputTemplateKey,
      @Value("${lab.base.url}") String labBaseUrl,
      @Value("${ship.base.url}") String shipBaseUrl,
      @Value("${stream.base.url}") String streamBaseUrl
  ) {
    this.audioBaseUrl = audioBaseUrl;
    this.context = context;
    this.ingestToken = ingestToken;
    this.inputTemplateKey = inputTemplateKey;
    this.labBaseUrl = labBaseUrl;
    this.shipBaseUrl = shipBaseUrl;
    this.streamBaseUrl = streamBaseUrl;

    HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClientFactory hubClientFactory = new HubClientFactoryImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory, audioDownloadRetries);
    ProjectManager projectManager = new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory);
    BroadcastFactory broadcastFactory = new BroadcastFactoryImpl();
    Telemetry telemetry = new TelemetryImpl();
    CraftFactory craftFactory = new CraftFactoryImpl();
    AudioLoader audioLoader = new AudioLoaderImpl(projectManager);
    AudioCache audioCache = new AudioCacheImpl(projectManager, audioLoader);
    NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
    FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
        nexusEntityStore,
        jsonapiPayloadFactory,
        jsonProvider
    );
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    fabricationManager = new FabricationManagerImpl(
        projectManager, telemetry,
        broadcastFactory,
        craftFactory,
        audioCache,
        fabricatorFactory,
        mixerFactory,
        nexusEntityStore
    );
  }

  @EventListener(ApplicationStartedEvent.class)
  public void start() {
    var workConfig = new FabricationSettings()
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
