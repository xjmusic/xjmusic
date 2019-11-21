//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.payload.serializer;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.xj.core.payload.Payload;
import io.xj.core.payload.PayloadDataType;

import java.lang.reflect.Type;

/**
 [#167276586] JSON API facilitates complex transactions
 <p>
 Payloads are Deserialized with Jackson; we're locked into that because that's what Jersey (our REST framework) uses.
 However, Payloads are Serialized with GSON for outbound data, because it's simpler.
 */
public class PayloadSerializer implements JsonSerializer<Payload> {

  @Override
  public JsonElement serialize(Payload src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();

    // Add data (one or many) if present
    if (PayloadDataType.HasOne == src.getDataType())
      obj.add(Payload.KEY_DATA, src.getDataOne().isPresent() ?
        context.serialize(src.getDataOne().get()) : JsonNull.INSTANCE);

    else if (PayloadDataType.HasMany == src.getDataType()) {
      JsonArray dataObjects = new JsonArray();
      src.getDataMany().forEach((value) -> dataObjects.add(context.serialize(value)));
      obj.add(Payload.KEY_DATA, dataObjects);
    }

    // Add links if present
    if (!src.getLinks().isEmpty()) {
      JsonObject linkMap = new JsonObject();
      src.getLinks().forEach((key, value) -> linkMap.add(key, context.serialize(value)));
      obj.add(Payload.KEY_LINKS, linkMap);
    }

    // Add included if present
    if (!src.getIncluded().isEmpty()) {
      JsonArray includedObjects = new JsonArray();
      src.getIncluded().forEach((value) -> includedObjects.add(context.serialize(value)));
      obj.add(Payload.KEY_INCLUDED, includedObjects);
    }

    // Add error if present
    if (!src.getErrors().isEmpty()) {
      JsonArray errorObjects = new JsonArray();
      src.getErrors().forEach((value) -> errorObjects.add(context.serialize(value)));
      obj.add(Payload.KEY_ERRORS, errorObjects);
    }

    return obj;
  }

}
