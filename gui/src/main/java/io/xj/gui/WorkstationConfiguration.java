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
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientImpl;
import io.xj.nexus.mixer.EnvelopeProvider;
import io.xj.nexus.mixer.EnvelopeProviderImpl;
import io.xj.nexus.mixer.MixerFactory;
import io.xj.nexus.mixer.MixerFactoryImpl;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.BroadcastFactoryImpl;
import io.xj.nexus.telemetry.Telemetry;
import io.xj.nexus.telemetry.TelemetryImpl;
import io.xj.nexus.work.WorkManager;
import io.xj.nexus.work.WorkManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class WorkstationConfiguration {
  private HttpClientProvider _httpClientProvider;
  private WorkManager _workManager = null;
  private EntityFactoryImpl _entityFactory;
  private JsonProviderImpl _jsonProvider;

  public WorkstationConfiguration(
  ) {
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
  public WorkManager workManager(
    EntityFactory entityFactory,
    JsonProvider jsonProvider,
    HttpClientProvider httpClientProvider
  ) {
    if (_workManager == null) {
      BroadcastFactory broadcastFactory = new BroadcastFactoryImpl();
      Telemetry telemetry = new TelemetryImpl();
      CraftFactory craftFactory = new CraftFactoryImpl();
      AudioCache audioCache = new AudioCacheImpl(httpClientProvider);
      NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
      JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
      HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
      FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
        nexusEntityStore,
        jsonapiPayloadFactory,
        jsonProvider
      );
      EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
      MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
      _workManager = new WorkManagerImpl(
        telemetry,
        broadcastFactory,
        craftFactory,
        audioCache,
        fabricatorFactory,
        hubClient,
        mixerFactory,
        nexusEntityStore
      );
    }
    return _workManager;
  }
}
