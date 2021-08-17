// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.hub_client.client;

import com.google.inject.AbstractModule;
import io.xj.lib.jsonapi.JsonapiModule;

/**
 Guice Module to inject the implementation of a Hub Client for connecting to Hub and accessing contents
 */
public class HubClientModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new JsonapiModule());
    bind(HubClient.class).to(HubClientImpl.class);
  }
}
