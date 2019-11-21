// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.testing;

import com.google.inject.AbstractModule;
import io.xj.core.testing.impl.IntegrationTestProviderImpl;

public class TestingModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(IntegrationTestProvider.class).to(IntegrationTestProviderImpl.class);
  }
}
