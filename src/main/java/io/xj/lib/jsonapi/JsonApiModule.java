// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.google.inject.AbstractModule;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.json.JsonModule;

/**
 Injection module for REST API entity
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class JsonApiModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new EntityModule());
    install(new JsonModule());
    bind(JsonapiPayloadFactory.class).to(JsonapiPayloadFactoryImpl.class);
    bind(JsonapiHttpResponseProvider.class).to(JsonapiHttpResponseProviderImpl.class);
  }
}
