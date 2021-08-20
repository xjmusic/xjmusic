//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;import com.google.inject.AbstractModule;

public class HubIntegrationTestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(HubIntegrationTestProvider.class).to(HubIntegrationTestProviderImpl.class);
  }
}
