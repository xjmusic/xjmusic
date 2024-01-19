package io.xj.gui.services;

import io.xj.nexus.NexusTopology;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.audio_cache.AudioCacheImpl;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.mixer.EnvelopeProvider;
import io.xj.nexus.mixer.EnvelopeProviderImpl;
import io.xj.nexus.mixer.MixerFactory;
import io.xj.nexus.mixer.MixerFactoryImpl;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.BroadcastFactoryImpl;
import io.xj.nexus.telemetry.Telemetry;
import io.xj.nexus.telemetry.TelemetryImpl;
import io.xj.nexus.work.FabricationManager;
import io.xj.nexus.work.FabricationManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkManagerConfig {

  private FabricationManager fabricationManager = null;

  private final ProjectManager projectManager;

  public WorkManagerConfig(
    ProjectManager projectManager
  ) {
    this.projectManager = projectManager;
  }

  @Bean
  public FabricationManager workManager() {
    if (fabricationManager == null) {
      BroadcastFactory broadcastFactory = new BroadcastFactoryImpl();
      Telemetry telemetry = new TelemetryImpl();
      CraftFactory craftFactory = new CraftFactoryImpl();
      AudioCache audioCache = new AudioCacheImpl(projectManager);
      JsonProvider jsonProvider = new JsonProviderImpl();
      EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
      NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
      JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
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
    return fabricationManager;
  }
}
