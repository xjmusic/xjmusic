// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.util.Text;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
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
  public static final String KEY_INSTRUMENT_ID = "instrumentId";
  public static final String KEY_WAVEFORM_KEY = "waveformKey";
  public static final String KEY_NAME = "name";
  public static final String KEY_START = "start";
  public static final String KEY_LENGTH = "length";
  public static final String KEY_TEMPO = "tempo";
  public static final String KEY_PITCH = "pitch";
  public static final String KEY_STATE = "state";
  private BigInteger instrumentId;
  private String waveformKey;
  private String name;
  private Double start;
  private Double length;
  private Double tempo;
  private Double pitch;
  private AudioState state;
  private String _stateString; // pending validation, copied to `state` field

  public Audio() {
  }

  public Audio(int id) {
    this.id = BigInteger.valueOf((long) id);
  }

  public Audio(BigInteger id) {
    this.id = id;
  }

  public BigInteger getInstrumentId() {
    return instrumentId;
  }

  public Audio setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
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
    _stateString = Text.toAlphabetical(stateString);
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
    if (null != waveformKey) {
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
  public BigInteger getParentId() {
    return instrumentId;
  }

  @Override
  public void validate() throws BusinessException {
    if (Objects.isNull(instrumentId))
      throw new BusinessException("Instrument ID is required.");

    // throws its own BusinessException on failure
    if (Objects.isNull(state))
      state = AudioState.validate(_stateString);

    if (Objects.isNull(name) || name.isEmpty())
      throw new BusinessException("Name is required.");

    if (Objects.isNull(waveformKey) || waveformKey.isEmpty())
      waveformKey = "";

    if (Objects.isNull(start))
      start = 0.0d;

    if (Objects.isNull(length))
      length = 0.0d;

    if (Objects.isNull(tempo))
      throw new BusinessException("Tempo is required.");

    if (Objects.isNull(pitch))
      throw new BusinessException("Root Pitch is required.");
  }

}
