// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.inject.AbstractModule;

public class HubAccessControlModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
    bind(HubAccessControlProvider.class).to(HubAccessControlProviderImpl.class);
    bind(HubAccessTokenGenerator.class).to(HubAccessTokenGeneratorImpl.class);
  }
}
