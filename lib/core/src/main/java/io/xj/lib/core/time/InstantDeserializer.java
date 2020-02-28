// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.time;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantDeserializer implements JsonDeserializer<Instant> {
  @Override
  public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
    String value = json.getAsString();
    return Instant.parse(value);
  }
}
