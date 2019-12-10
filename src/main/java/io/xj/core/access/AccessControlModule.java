// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import com.google.inject.AbstractModule;

public class AccessControlModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AccessControlProvider.class).to(AccessControlProviderImpl.class);
    bind(AccessLogFilterProvider.class).to(AccessLogFilterProviderImpl.class);
    bind(AccessTokenAuthFilter.class).to(AccessTokenAuthFilterImpl.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
  }
}
