package io.xj.gui;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkstationConfiguration {

  @Bean
  public JsonProvider jsonProvider() {
    return new JsonProviderImpl();
  }

  @Bean
  public HttpClientProvider httpClientProvider() {
    return new HttpClientProviderImpl();
  }

  @Bean
  public JsonapiPayloadFactory jsonapiPayloadFactory(
    EntityFactory entityFactory
  ) {
    return new JsonapiPayloadFactoryImpl(entityFactory);
  }

  @Bean
  public HubClientFactory hubClientFactory(
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    return new HubClientFactoryImpl(
      httpClientProvider,
      jsonProvider,
      jsonapiPayloadFactory
    );
  }

  @Bean
  public EntityFactory entityFactory(
    JsonProvider jsonProvider
  ) {
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    return entityFactory;
  }

  @Bean
  public ProjectManager projectManager(
    JsonProvider jsonProvider,
    EntityFactory entityFactory,
    HttpClientProvider httpClientProvider,
    HubClientFactory hubClientFactory,
    @Value("${audio.download.retries}") int downloadAudioRetries,
    @Value("${audio.upload.retries}") int uploadAudioRetries
  ) {
    return new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory, downloadAudioRetries, uploadAudioRetries);
  }

  @Bean
  public AudioLoader audioLoader(
    ProjectManager projectManager
  ) {
    return new AudioLoaderImpl(projectManager);
  }

  @Bean
  public FabricationManager workManager(
    ProjectManager projectManager,
    EntityFactory entityFactory,
    JsonProvider jsonProvider,
    AudioLoader audioLoader
  ) {
    BroadcastFactory broadcastFactory = new BroadcastFactoryImpl();
    Telemetry telemetry = new TelemetryImpl();
    CraftFactory craftFactory = new CraftFactoryImpl();
    AudioCache audioCache = new AudioCacheImpl(projectManager, audioLoader);
    NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
      nexusEntityStore,
      jsonapiPayloadFactory,
      jsonProvider
    );
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
    return new FabricationManagerImpl(
      projectManager,
      telemetry,
      broadcastFactory,
      craftFactory,
      audioCache,
      fabricatorFactory,
      mixerFactory,
      nexusEntityStore
    );
  }
}
