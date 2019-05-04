//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.arrangement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class ArrangementSerializer implements JsonSerializer<Arrangement> {
  private static final String CHOICE_UUID = "choiceUuid";
  private static final String UUID = "uuid";
  private static final String INSTRUMENT_ID = "instrumentId";
  private static final String SEGMENT_ID = "segmentId";
  private static final String VOICE_ID = "voiceId";

  @Override
  public JsonElement serialize(Arrangement src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(UUID, context.serialize(src.getUuid()));
    obj.add(SEGMENT_ID, context.serialize(src.getSegmentId()));
    obj.add(CHOICE_UUID, context.serialize(src.getChoiceUuid()));
    obj.add(VOICE_ID, context.serialize(src.getVoiceId()));
    obj.add(INSTRUMENT_ID, context.serialize(src.getInstrumentId()));
    return obj;
  }
}
