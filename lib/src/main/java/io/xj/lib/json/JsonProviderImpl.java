// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.entity.InstantDeserializer;
import io.xj.lib.entity.InstantSerializer;

import java.time.Instant;

@Singleton
public class JsonProviderImpl implements JsonProvider {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  public JsonProviderImpl() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    objectMapper.registerModule(module);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
