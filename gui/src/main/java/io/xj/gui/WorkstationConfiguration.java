package io.xj.gui;

import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.audio_cache.AudioCacheImpl;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.http.HttpClientProviderImpl;
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

import java.util.Objects;

@Configuration
public class WorkstationConfiguration {
  private final int downloadAudioRetries;
  private HttpClientProvider _httpClientProvider;
  private FabricationManager _fabricationManager = null;
  private ProjectManager _projectManager = null;
  private EntityFactoryImpl _entityFactory;
  private JsonProviderImpl _jsonProvider;

  public WorkstationConfiguration(
    @Value("${download.audio.retries}") int downloadAudioRetries
  ) {
    this.downloadAudioRetries = downloadAudioRetries;
  }

  @Bean
  public JsonProvider jsonProvider() {
    if (Objects.isNull(_jsonProvider)) {
      _jsonProvider = new JsonProviderImpl();
    }
    return _jsonProvider;
  }

  @Bean
  public HttpClientProvider httpClientProvider() {
    if (_httpClientProvider == null) {
      _httpClientProvider = new HttpClientProviderImpl();
    }
    return _httpClientProvider;
  }

  @Bean
  public EntityFactory entityFactory(
    JsonProvider jsonProvider
  ) {
    if (Objects.isNull(_entityFactory)) {
      _entityFactory = new EntityFactoryImpl(jsonProvider);
      HubTopology.buildHubApiTopology(_entityFactory);
      NexusTopology.buildNexusApiTopology(_entityFactory);
    }
    return _entityFactory;
  }

  @Bean
  public ProjectManager projectManager(
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    EntityFactory entityFactory
  ) {
    if (Objects.isNull(_projectManager)) {
      _projectManager = new ProjectManagerImpl(httpClientProvider, jsonProvider, entityFactory, downloadAudioRetries);
    }
    return _projectManager;
  }

  @Bean
  public FabricationManager workManager(
    ProjectManager projectManager,
    EntityFactory entityFactory,
    JsonProvider jsonProvider
  ) {
    if (_fabricationManager == null) {
      BroadcastFactory broadcastFactory = new BroadcastFactoryImpl();
      Telemetry telemetry = new TelemetryImpl();
      CraftFactory craftFactory = new CraftFactoryImpl();
      AudioCache audioCache = new AudioCacheImpl(projectManager);
      NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
      JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
      FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
        nexusEntityStore,
        jsonapiPayloadFactory,
        jsonProvider
      );
      EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
      MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
      _fabricationManager = new FabricationManagerImpl(
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
    return _fabricationManager;
  }
}
