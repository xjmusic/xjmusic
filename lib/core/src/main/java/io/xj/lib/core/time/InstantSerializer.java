// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.time;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class InstantSerializer implements JsonSerializer<Instant> {
  @Override
  public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.truncatedTo(ChronoUnit.MICROS).toString());
  }
}
