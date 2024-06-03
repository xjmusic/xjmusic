package io.xj.gui;

import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.NexusTopology;
import io.xj.engine.audio.AudioCache;
import io.xj.engine.audio.AudioCacheImpl;
import io.xj.engine.audio.AudioLoader;
import io.xj.engine.audio.AudioLoaderImpl;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.engine.http.HttpClientProvider;
import io.xj.engine.http.HttpClientProviderImpl;
import io.xj.engine.hub_client.HubClientFactory;
import io.xj.engine.hub_client.HubClientFactoryImpl;
import io.xj.engine.mixer.EnvelopeProvider;
import io.xj.engine.mixer.EnvelopeProviderImpl;
import io.xj.engine.mixer.MixerFactory;
import io.xj.engine.mixer.MixerFactoryImpl;
import io.xj.engine.fabricator.FabricationEntityStore;
import io.xj.engine.fabricator.FabricationEntityStoreImpl;
import io.xj.engine.project.ProjectManager;
import io.xj.engine.project.ProjectManagerImpl;
import io.xj.engine.ship.broadcast.BroadcastFactory;
import io.xj.engine.ship.broadcast.BroadcastFactoryImpl;
import io.xj.engine.telemetry.Telemetry;
import io.xj.engine.telemetry.TelemetryImpl;
import io.xj.engine.work.FabricationManager;
import io.xj.engine.work.FabricationManagerImpl;
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
      JsonapiPayloadFactory jsonapiPayloadFactory,
      @Value("${demo.audioDownloadRetries}") int audioDownloadRetries
  ) {
    return new HubClientFactoryImpl(
        httpClientProvider,
        jsonProvider,
        jsonapiPayloadFactory,
        audioDownloadRetries
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
      HubClientFactory hubClientFactory
  ) {
    return new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory);
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
    FabricationEntityStore fabricationEntityStore = new FabricationEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
      fabricationEntityStore,
        jsonapiPayloadFactory,
        jsonProvider
    );
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
    return new FabricationManagerImpl(
      telemetry,
        broadcastFactory,
        craftFactory,
        audioCache,
        fabricatorFactory,
        mixerFactory,
      fabricationEntityStore
    );
  }
}
