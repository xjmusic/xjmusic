//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport.impl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.xj.core.exception.CoreException;
import io.xj.core.util.TimestampUTC;

import java.lang.reflect.Type;
import java.sql.Timestamp;

public class TimestampDeserializer implements JsonDeserializer<Timestamp> {
  @Override
  public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String value = json.getAsString();
    try {
      return TimestampUTC.valueOf(value);

    } catch (CoreException e) {
      throw new JsonParseException(String.format("Invalid timestamp value: %s", value), e);
    }
  }
}
