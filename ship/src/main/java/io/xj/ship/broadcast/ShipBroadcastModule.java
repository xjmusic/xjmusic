package io.xj.ship.broadcast;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.nexus.persistence.NexusPersistenceModule;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.ship.source.*;

public class ShipBroadcastModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ChunkManager.class).to(ChunkManagerImpl.class);
    bind(PlaylistProvider.class).to(PlaylistProviderImpl.class);
    install(new FactoryModuleBuilder()
      .implement(Chunk.class, Chunk.class)
      .build(ShipBroadcastFactory.class));
    install(new ShipSourceModule());
    install(new EntityModule());
    install(new NexusPersistenceModule());
    install(new FileStoreModule());
    install(new NotificationModule());
  }
}

