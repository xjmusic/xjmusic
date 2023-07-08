// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.xj.lib.entity.InstantDeserializer;
import io.xj.lib.entity.InstantSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class JsonProviderImpl implements JsonProvider {
  final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  public JsonProviderImpl() {
    mapper.registerModule(buildInstantSerDesModule());
    mapper.registerModule(buildJavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public ObjectMapper getMapper() {
    return mapper;
  }

  Module buildInstantSerDesModule() {
    SimpleModule mod = new SimpleModule();
    mod.addSerializer(Instant.class, new InstantSerializer());
    mod.addDeserializer(Instant.class, new InstantDeserializer());
    return mod;
  }

  Module buildJavaTimeModule() {
    JavaTimeModule mod = new JavaTimeModule();
    mod.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
    return mod;
  }
}
