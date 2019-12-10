// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external;

import com.google.inject.AbstractModule;

public class ExternalResourceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AmazonProvider.class).to(AmazonProviderImpl.class);
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
  }
}
