package io.xj.ship.source;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.nexus.persistence.NexusPersistenceModule;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.ship.broadcast.Chunk;
import io.xj.ship.broadcast.ChunkManager;
import io.xj.ship.broadcast.ChunkManagerImpl;

public class ShipSourceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SegmentManager.class).to(SegmentManagerImpl.class);
    bind(SegmentAudioManager.class).to(SegmentAudioManagerImpl.class);
    install(new FactoryModuleBuilder()
      .implement(SegmentAudio.class, SegmentAudio.class)
      .build(ShipSourceFactory.class));

    install(new EntityModule());
    install(new NexusPersistenceModule());
    install(new FileStoreModule());
    install(new NotificationModule());
  }
}

