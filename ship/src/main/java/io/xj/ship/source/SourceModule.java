// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.json.JsonModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.nexus.persistence.NexusPersistenceModule;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;

public class SourceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SegmentManager.class).to(SegmentManagerImpl.class);
    bind(SegmentAudioManager.class).to(SegmentAudioManagerImpl.class);
    install(new FactoryModuleBuilder()
      .implement(ChainLoader.class, ChainLoaderImpl.class)
      .implement(SegmentAudio.class, SegmentAudio.class)
      .implement(SegmentLoader.class, SegmentLoaderImpl.class)
      .build(SourceFactory.class));

    install(new JsonModule());
    install(new JsonapiModule());
    install(new EntityModule());
    install(new NexusPersistenceModule());
    install(new FileStoreModule());
    install(new NotificationModule());
  }
}

