// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.AUDIO;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Audio extends Entity {
  public static final String KEY_ONE = "audio";
  public static final String KEY_MANY = "audios";
  public static final String FILE_EXTENSION = "wav";
  private ULong instrumentId;
  private String waveformKey;
  private String name;
  private Double start;
  private Double length;
  private Double tempo;
  private Double pitch;

  public ULong getInstrumentId() {
    return instrumentId;
  }

  public Audio setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = ULong.valueOf(instrumentId);
    return this;
  }

  public String getWaveformKey() {
    return waveformKey;
  }

  public Audio setWaveformKey(String waveformKey) {
    if (waveformKey != null) {
      this.waveformKey = waveformKey.trim();
    }
    return this;
  }

  public String getName() {
    return name;
  }

  public Audio setName(String name) {
    this.name = name;
    return this;
  }

  public Double getStart() {
    return start;
  }

  public Audio setStart(Double start) {
    this.start = start;
    return this;
  }

  public Double getLength() {
    return length;
  }

  public Audio setLength(Double length) {
    this.length = length;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Audio setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  public Double getPitch() {
    return pitch;
  }

  public Audio setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.waveformKey == null || this.waveformKey.length() == 0) {
      this.waveformKey = "";
//      throw new BusinessException("Waveform URL is required.");
    }
    if (this.start == null) {
      this.start = 0d;
//      throw new BusinessException("Start is required.");
    }
    if (this.length == null) {
      this.length = 0d;
//      throw new BusinessException("Length is required.");
    }
    if (this.tempo == null) {
      this.tempo = 0d;
      throw new BusinessException("Tempo is required.");
    }
    if (this.pitch == null) {
      this.pitch = 0d;
      throw new BusinessException("Root Pitch is required.");
    }
  }

  @Override
  public Audio setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(AUDIO.ID);
    instrumentId = record.get(AUDIO.INSTRUMENT_ID);
    waveformKey = record.get(AUDIO.WAVEFORM_KEY);
    name = record.get(AUDIO.NAME);
    start = record.get(AUDIO.START);
    length = record.get(AUDIO.LENGTH);
    tempo = record.get(AUDIO.TEMPO);
    pitch = record.get(AUDIO.PITCH);
    createdAt = record.get(AUDIO.CREATED_AT);
    updatedAt = record.get(AUDIO.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(AUDIO.INSTRUMENT_ID, instrumentId);
    fieldValues.put(AUDIO.NAME, name);
    fieldValues.put(AUDIO.START, start);
    fieldValues.put(AUDIO.LENGTH, length);
    fieldValues.put(AUDIO.TEMPO, tempo);
    fieldValues.put(AUDIO.PITCH, pitch);
    // Excluding AUDIO.WAVEFORM_KEY a.k.a. waveformKey because that is read-only
    return fieldValues;
  }

}
