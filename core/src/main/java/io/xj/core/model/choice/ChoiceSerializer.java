//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class ChoiceSerializer implements JsonSerializer<Choice> {
  private static final String UUID = "uuid";
  private static final String SEGMENT_ID = "segmentId";
  private static final String SEQUENCE_ID = "sequenceId";
  private static final String SEQUENCE_PATTERN_ID = "sequencePatternId";
  private static final String TRANSPOSE = "transpose";
  private static final String TYPE = "type";

  @Override
  public JsonElement serialize(Choice src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(UUID, context.serialize(src.getUuid()));
    obj.add(TYPE, context.serialize(src.getType()));
    switch (src.getType()) {
      case Macro:
      case Main:
        obj.add(SEQUENCE_PATTERN_ID, context.serialize(src.getSequencePatternId()));
        break;
      case Rhythm:
      case Detail:
        obj.add(SEQUENCE_ID, context.serialize(src.getSequenceId()));
        break;
    }
    obj.add(SEGMENT_ID, context.serialize(src.getSegmentId()));
    obj.add(TRANSPOSE, context.serialize(src.getTranspose()));
    return obj;
  }
}
