//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.impl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.xj.core.exception.CoreException;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;

import java.lang.reflect.Type;

public class SegmentDeserializer implements JsonDeserializer<Segment> {
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
  private final SegmentFactory segmentFactory;

  public SegmentDeserializer(SegmentFactory segmentFactory) {
    this.segmentFactory = segmentFactory;
  }

  @Override
  public Segment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    Segment segment = segmentFactory.newSegment(obj.get(ID).getAsBigInteger());
    segment.setChainId(obj.get(CHAIN_ID).getAsBigInteger());
    segment.setOffset(obj.get(OFFSET).getAsBigInteger());
    try {
      segment.setState(obj.get(STATE).getAsString());
    } catch (CoreException e) {
      throw new JsonParseException("Cannot parse segment with invalid type", e);
    }
    segment.setBeginAt(obj.get(BEGIN_AT).getAsString());
    segment.setEndAt(obj.get(END_AT).getAsString());
    segment.setTotal(obj.get(TOTAL).getAsInt());
    segment.setDensity(obj.get(DENSITY).getAsDouble());
    segment.setKey(obj.get(KEY).getAsString());
    segment.setTempo(obj.get(TEMPO).getAsDouble());
    segment.setCreatedAt(obj.get(CREATED_AT).getAsString());
    segment.setUpdatedAt(obj.get(UPDATED_AT).getAsString());
    segment.setWaveformKey(obj.get(WAVEFORM_KEY).getAsString());
    return segment;
  }
}
