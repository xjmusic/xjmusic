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
public class PayloadErrorSerializer implements JsonSerializer<PayloadError> {

  @Override
  public JsonElement serialize(PayloadError src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();

    // Add id if present
    if (Objects.nonNull(src.getId()) && !src.getId().isEmpty())
      obj.add(PayloadError.KEY_ID, context.serialize(src.getId()));

    // Add code if present
    if (Objects.nonNull(src.getCode()) && !src.getCode().isEmpty())
      obj.add(PayloadError.KEY_CODE, context.serialize(src.getCode()));

    // Add title if present
    if (Objects.nonNull(src.getTitle()) && !src.getTitle().isEmpty())
      obj.add(PayloadError.KEY_TITLE, context.serialize(src.getTitle()));

    // Add detail if present
    if (Objects.nonNull(src.getDetail()) && !src.getDetail().isEmpty())
      obj.add(PayloadError.KEY_DETAIL, context.serialize(src.getDetail()));

    // Add links if present
    if (!src.getLinks().isEmpty()) {
      JsonObject linkMap = new JsonObject();
      src.getLinks().forEach((key, value) -> linkMap.add(key, context.serialize(value)));
      obj.add(PayloadError.KEY_LINKS, linkMap);
    }

    return obj;
  }

}
