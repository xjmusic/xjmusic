// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.AbstractModule;
import io.xj.core.transport.impl.GsonProviderImpl;
import io.xj.core.transport.impl.HttpResponseProviderImpl;
import io.xj.core.transport.impl.HttpServerProviderImpl;
import io.xj.core.transport.impl.ResourceConfigProviderImpl;
import io.xj.core.transport.impl.StatsProviderImpl;

public class TransportModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GsonProvider.class).to(GsonProviderImpl.class);
    bind(HttpResponseProvider.class).to(HttpResponseProviderImpl.class);
    bind(HttpServerProvider.class).to(HttpServerProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(ResourceConfigProvider.class).to(ResourceConfigProviderImpl.class);
    bind(StatsProvider.class).to(StatsProviderImpl.class);
  }
}
