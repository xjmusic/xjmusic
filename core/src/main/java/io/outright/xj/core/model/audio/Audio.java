// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.audio;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.AUDIO;

public class Audio extends Entity {

  /**
   * Instrument
   */
  private ULong instrumentId;

  public ULong getInstrumentId() {
    return instrumentId;
  }

  public Audio setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = ULong.valueOf(instrumentId);
    return this;
  }

  /**
   * WaveformUrl
   */
  private String waveformUrl;

  public String getWaveformUrl() {
    return waveformUrl;
  }

  public Audio setWaveformUrl(String waveformUrl) {
    if (waveformUrl != null) {
      this.waveformUrl = waveformUrl.trim();
    }
    return this;
  }

  /**
   * Name
   */
  private String name;

  public String getName() {
    return name;
  }

  public Audio setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Start
   */
  private Double start;

  public Double getStart() {
    return start;
  }

  public Audio setStart(Double start) {
    this.start = start;
    return this;
  }

  /**
   * Length
   */
  private Double length;

  public Double getLength() {
    return length;
  }

  public Audio setLength(Double length) {
    this.length = length;
    return this;
  }

  /**
   * Tempo
   */
  private Double tempo;

  public Double getTempo() {
    return tempo;
  }

  public Audio setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   * Pitch
   */
  private Double pitch;

  public Double getPitch() {
    return pitch;
  }

  public Audio setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  @Override
  public void validate() throws BusinessException {
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.waveformUrl == null || this.waveformUrl.length() == 0) {
      throw new BusinessException("Waveform URL is required.");
    }
    if (this.start == null || this.start == 0) {
      throw new BusinessException("Start is required.");
    }
    if (this.length == null || this.length == 0) {
      throw new BusinessException("Length is required.");
    }
    if (this.tempo == null || this.tempo == 0) {
      throw new BusinessException("Tempo is required.");
    }
    if (this.pitch == null || this.pitch == 0) {
      throw new BusinessException("Pitch is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(AUDIO.INSTRUMENT_ID, instrumentId);
    fieldValues.put(AUDIO.NAME, name);
    fieldValues.put(AUDIO.WAVEFORM_URL, waveformUrl);
    fieldValues.put(AUDIO.START, start);
    fieldValues.put(AUDIO.LENGTH, length);
    fieldValues.put(AUDIO.TEMPO, tempo);
    fieldValues.put(AUDIO.PITCH, pitch);
    return fieldValues;
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "audio";
  public static final String KEY_MANY = "audios";

}
