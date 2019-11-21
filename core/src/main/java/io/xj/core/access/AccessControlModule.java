// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import com.google.inject.AbstractModule;
import io.xj.core.access.impl.AccessControlProviderImpl;
import io.xj.core.access.impl.AccessLogFilterProviderImpl;
import io.xj.core.access.impl.AccessTokenAuthFilterImpl;
import io.xj.core.access.token.TokenGenerator;
import io.xj.core.access.token.TokenGeneratorImpl;

public class AccessControlModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AccessControlProvider.class).to(AccessControlProviderImpl.class);
    bind(AccessLogFilterProvider.class).to(AccessLogFilterProviderImpl.class);
    bind(AccessTokenAuthFilter.class).to(AccessTokenAuthFilterImpl.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
  }
}
