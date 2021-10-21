// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship;

import com.google.inject.AbstractModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.ship.source.SourceModule;
import io.xj.ship.work.ShipWorkModule;

public class ShipIntegrationTestModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new MixerModule());
    install(new SourceModule());
    install(new ShipWorkModule());
    install(new JsonapiModule());
    install(new FileStoreModule());
    bind(ShipIntegrationTestProvider.class).to(ShipIntegrationTestProviderImpl.class);
  }
}
