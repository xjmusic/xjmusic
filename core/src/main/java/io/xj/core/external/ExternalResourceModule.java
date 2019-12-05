// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external;

import com.google.inject.AbstractModule;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.external.amazon.AmazonProviderImpl;
import io.xj.core.external.google.GoogleHttpProvider;
import io.xj.core.external.google.GoogleHttpProviderImpl;
import io.xj.core.external.google.GoogleProvider;
import io.xj.core.external.google.GoogleProviderImpl;

public class ExternalResourceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AmazonProvider.class).to(AmazonProviderImpl.class);
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
  }
}
