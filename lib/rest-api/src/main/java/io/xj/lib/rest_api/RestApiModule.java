// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.AbstractModule;

/**
 Injection module for REST API entity
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class RestApiModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PayloadFactory.class).to(PayloadFactoryImpl.class);
    bind(ApiUrlProvider.class).to(ApiUrlProviderImpl.class);
    bind(HttpResponseProvider.class).to(HttpResponseProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
  }

}
