// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.payload;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 [#167276586] JSON API facilitates complex transactions
 <p>
 Payloads are Deserialized with Jackson; we're locked into that because that's what Jersey (our REST framework) uses.
 However, Payloads are Serialized with GSON for outbound data, because it's simpler.
 */
public class PayloadObjectSerializer implements JsonSerializer<PayloadObject> {

  @Override
  public JsonElement serialize(PayloadObject src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();

    // Add id if present
    if (Objects.nonNull(src.getId()) && !src.getId().isEmpty())
      obj.add(PayloadObject.KEY_ID, context.serialize(src.getId()));

    // Add type if present
    if (Objects.nonNull(src.getType()) && !src.getType().isEmpty())
      obj.add(PayloadObject.KEY_TYPE, context.serialize(src.getType()));

    // Add links if present
    if (!src.getLinks().isEmpty()) {
      JsonObject linkMap = new JsonObject();
      src.getLinks().forEach((key, value) -> linkMap.add(key, context.serialize(value)));
      obj.add(PayloadObject.KEY_LINKS, linkMap);
    }

    // Add relationships if present
    if (!src.getRelationships().isEmpty()) {
      JsonObject relationshipMap = new JsonObject();
      src.getRelationships().forEach((key, value) -> relationshipMap.add(key, context.serialize(value)));
      obj.add(PayloadObject.KEY_RELATIONSHIPS, relationshipMap);
    }

    // Add attributes if present
    if (!src.getAttributes().isEmpty()) {
      JsonObject attributeMap = new JsonObject();
      src.getAttributes().forEach((key, value) -> attributeMap.add(key, context.serialize(value)));
      obj.add(PayloadObject.KEY_ATTRIBUTES, attributeMap);
    }

    return obj;
  }

}
