// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application;

import io.outright.xj.core.application.server.BaseUrlProvider;
import io.outright.xj.core.application.server.BaseUrlProviderImpl;
import io.outright.xj.core.application.server.HttpServerProvider;
import io.outright.xj.core.application.server.HttpServerProviderImpl;
import io.outright.xj.core.application.server.LogFilterProvider;
import io.outright.xj.core.application.server.LogFilterProviderImpl;
import io.outright.xj.core.application.server.ResourceConfigProvider;
import io.outright.xj.core.application.server.ResourceConfigProviderImpl;

import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
  protected void configure() {
    bind(Application.class).to(ApplicationImpl.class);
    bind(BaseUrlProvider.class).to(BaseUrlProviderImpl.class);
    bind(HttpServerProvider.class).to(HttpServerProviderImpl.class);
    bind(ResourceConfigProvider.class).to(ResourceConfigProviderImpl.class);
    bind(LogFilterProvider.class).to(LogFilterProviderImpl.class);
  }
}
