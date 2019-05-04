//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_chord;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SegmentChordSerializer implements JsonSerializer<SegmentChord> {
  private static final String UUID = "uuid";
  private static final String NAME = "name";
  private static final String SEGMENT_ID = "segmentId";
  private static final String POSITION = "position";

  @Override
  public JsonElement serialize(SegmentChord src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(UUID, context.serialize(src.getUuid()));
    obj.add(NAME, context.serialize(src.getName()));
    obj.add(POSITION, context.serialize(src.getPosition()));
    obj.add(SEGMENT_ID, context.serialize(src.getSegmentId()));
    return obj;
  }
}
