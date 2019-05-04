//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pick;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class PickSerializer implements JsonSerializer<Pick> {
  private static final String AMPLITUDE = "amplitude";
  private static final String ARRANGEMENT_UUID = "arrangementUuid";
  private static final String AUDIO_ID = "audioId";
  private static final String UUID = "uuid";
  private static final String INFLECTION = "inflection";
  private static final String LENGTH = "length";
  private static final String PATTERN_EVENT_ID = "patternEventId";
  private static final String PITCH = "pitch";
  private static final String SEGMENT_ID = "segmentId";
  private static final String START = "start";
  private static final String VOICE_ID = "voiceId";

  @Override
  public JsonElement serialize(Pick src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(UUID, context.serialize(src.getUuid()));
    obj.add(SEGMENT_ID, context.serialize(src.getSegmentId()));
    obj.add(ARRANGEMENT_UUID, context.serialize(src.getArrangementUuid()));
    obj.add(AUDIO_ID, context.serialize(src.getAudioId()));
    obj.add(PATTERN_EVENT_ID, context.serialize(src.getPatternEventId()));
    obj.add(START, context.serialize(src.getStart()));
    obj.add(LENGTH, context.serialize(src.getLength()));
    obj.add(AMPLITUDE, context.serialize(src.getAmplitude()));
    obj.add(PITCH, context.serialize(src.getPitch()));
    obj.add(INFLECTION, context.serialize(src.getInflection()));
    obj.add(VOICE_ID, context.serialize(src.getVoiceId()));
    return obj;
  }
}
