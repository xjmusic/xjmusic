// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.transport.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.sql.Timestamp;

public class TimestampSerializer implements JsonSerializer<Timestamp> {
  public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(String.format("%sZ", src));
  }
}
