// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.persistence;

import com.google.inject.AbstractModule;
import io.xj.lib.entity.EntityModule;

public class ShipEntityStoreModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new EntityModule());
    bind(ShipEntityStore.class).to(ShipEntityStoreImpl.class);
  }

}
