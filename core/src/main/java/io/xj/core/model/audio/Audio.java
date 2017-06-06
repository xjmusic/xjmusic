// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.audio;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.AUDIO;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Audio extends Entity {
  public static final String KEY_ONE = "audio";
  public static final String KEY_MANY = "audios";
  public static final String FILE_EXTENSION = "wav";
  private ULong instrumentId;
  private String waveformKey;
  private String name;
  private AudioState state;
  private Double start;
  private Double length;
  private Double tempo;
  private Double pitch;
  private String _stateString; // pending validation, copied to `state` field

  public ULong getInstrumentId() {
    return instrumentId;
  }

  public Audio setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = ULong.valueOf(instrumentId);
    return this;
  }

  public AudioState getState() {
    return state;
  }

  /**
   This sets the state String, however the value will remain null
   until validate() is called and the value is cast to enum

   @param stateString pending validation
   */
  public Audio setState(String stateString) {
    this._stateString = Text.alphabetical(stateString);
    return this;
  }

  public Audio setStateEnum(AudioState state) {
    this.state = state;
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
    if (this.instrumentId == null)
      throw new BusinessException("Instrument ID is required.");

    // throws its own BusinessException on failure
    this.state = AudioState.validate(_stateString);

    if (this.name == null || this.name.length() == 0)
      throw new BusinessException("Name is required.");

    if (this.waveformKey == null || this.waveformKey.length() == 0)
      this.waveformKey = "";

    if (this.start == null)
      this.start = 0d;

    if (this.length == null)
      this.length = 0d;

    if (this.tempo == null)
      throw new BusinessException("Tempo is required.");

    if (this.pitch == null)
      throw new BusinessException("Root Pitch is required.");
  }

  @Override
  public Audio setFromRecord(Record record) {
    if (Objects.isNull(record))
      return null;

    if (Objects.nonNull(record.field(AUDIO.STATE)))
      state = AudioState.valueOf(record.get(AUDIO.STATE));

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
    fieldValues.put(AUDIO.STATE, state);
    fieldValues.put(AUDIO.START, start);
    fieldValues.put(AUDIO.LENGTH, length);
    fieldValues.put(AUDIO.TEMPO, tempo);
    fieldValues.put(AUDIO.PITCH, pitch);
    // Excluding AUDIO.WAVEFORM_KEY a.k.a. waveformKey because that is read-only
    return fieldValues;
  }

}
