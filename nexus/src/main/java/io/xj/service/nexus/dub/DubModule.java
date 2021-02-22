// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.mixer.MixerModule;

public class DubModule extends AbstractModule {

  protected void configure() {
    install(new MixerModule());
    bind(DubAudioCache.class).to(DubAudioCacheImpl.class);
    install(new FactoryModuleBuilder()
      .implement(DubMaster.class, DubMasterImpl.class)
      .implement(DubShip.class, DubShipImpl.class)
      .build(DubFactory.class));
    install(new FactoryModuleBuilder()
      .implement(DubAudioCacheItem.class, DubAudioCacheItem.class)
      .build(DubAudioCacheItemFactory.class));
  }

}
