// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.json;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.inject.AbstractModule;

/**
 JSON interaction module
 <p>
 Created by Charney Kaye on 2020/06/22
 */
public class JsonModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(JsonProvider.class).to(JsonProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
  }
}
