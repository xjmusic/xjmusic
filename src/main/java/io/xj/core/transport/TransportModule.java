// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.AbstractModule;

public class TransportModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(GsonProvider.class).to(GsonProviderImpl.class);
    bind(ApiUrlProvider.class).to(ApiUrlProviderImpl.class);
    bind(HttpResponseProvider.class).to(HttpResponseProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(StatsProvider.class).to(StatsProviderImpl.class);
  }
}
