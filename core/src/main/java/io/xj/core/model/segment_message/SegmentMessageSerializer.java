//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SegmentMessageSerializer implements JsonSerializer<SegmentMessage> {
  private static final String UUID = "uuid";
  private static final String SEGMENT_ID = "segmentId";
  private static final String TYPE = "type";
  private static final String BODY = "body";

  @Override
  public JsonElement serialize(SegmentMessage src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(UUID, context.serialize(src.getUuid()));
    obj.add(TYPE, context.serialize(src.getType()));
    obj.add(BODY, context.serialize(src.getBody()));
    obj.add(SEGMENT_ID, context.serialize(src.getSegmentId()));
    return obj;
  }
}
