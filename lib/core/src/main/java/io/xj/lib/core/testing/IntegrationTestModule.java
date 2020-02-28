// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.testing;

import com.google.inject.AbstractModule;

public class IntegrationTestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(IntegrationTestProvider.class).to(IntegrationTestProviderImpl.class);
  }
}
