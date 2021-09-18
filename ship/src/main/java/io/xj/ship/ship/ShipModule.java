// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.ship;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.mixer.MixerModule;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class ShipModule extends AbstractModule {

  protected void configure() {
    install(new MixerModule());
    install(new FactoryModuleBuilder()
      .implement(Ship.class, ShipImpl.class)
      .build(ShipFactory.class));
  }

}
