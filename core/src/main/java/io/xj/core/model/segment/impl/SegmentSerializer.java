//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.xj.core.model.segment.Segment;

import java.lang.reflect.Type;

public class SegmentSerializer implements JsonSerializer<Segment> {
  private static final String ID = "id";
  private static final String CHAIN_ID = "chainId";
  private static final String OFFSET = "offset";
  private static final String STATE = "state";
  private static final String BEGIN_AT = "beginAt";
  private static final String END_AT = "endAt";
  private static final String TOTAL = "total";
  private static final String DENSITY = "density";
  private static final String KEY = "key";
  private static final String TEMPO = "tempo";
  private static final String CREATED_AT = "createdAt";
  private static final String UPDATED_AT = "updatedAt";
  private static final String WAVEFORM_KEY = "waveformKey";
  @Override
  public JsonElement serialize(Segment src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(ID, context.serialize(src.getId()));
    obj.add(CHAIN_ID, context.serialize(src.getChainId()));
    obj.add(OFFSET, context.serialize(src.getOffset()));
    obj.add(STATE, context.serialize(src.getState()));
    obj.add(BEGIN_AT, context.serialize(src.getBeginAt()));
    obj.add(END_AT, context.serialize(src.getEndAt()));
    obj.add(TOTAL, context.serialize(src.getTotal()));
    obj.add(DENSITY, context.serialize(src.getDensity()));
    obj.add(KEY, context.serialize(src.getKey()));
    obj.add(TEMPO, context.serialize(src.getTempo()));
    obj.add(CREATED_AT, context.serialize(src.getCreatedAt()));
    obj.add(UPDATED_AT, context.serialize(src.getUpdatedAt()));
    obj.add(WAVEFORM_KEY, context.serialize(src.getWaveformKey()));
    return obj;
  }
}
