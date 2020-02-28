// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;

import java.util.Objects;
import java.util.UUID;

/**
 [#166708597] Audio model handles all of its own entities
 */
public class InstrumentAudio extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("waveformKey")
    .add("name")
    .add("start")
    .add("length")
    .add("tempo")
    .add("pitch")
    .add("density")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Instrument.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(InstrumentAudioChord.class)
    .add(InstrumentAudioEvent.class)
    .build();
  private String waveformKey;
  private String name;
  private Double start;
  private Double length;
  private Double tempo;
  private Double pitch;
  private Double density;
  private UUID instrumentId;

  /**
   Create a new instrument audio

   @return audio
   */
  public static InstrumentAudio create() {
    return (InstrumentAudio) new InstrumentAudio().setId(UUID.randomUUID());
  }

  /**
   Create a new instrument audio

   @param name of audio
   @return audio
   */
  public static InstrumentAudio create(Instrument instrument, String name) {
    return create()
      .setInstrumentId(instrument.getId())
      .setName(name);
  }

  /**
   Create a new Audio

   @param name        of audio
   @param waveformKey of audio
   @param start       of audio
   @param length      of audio
   @param tempo       of audio
   @param pitch       of audio
   @param density     of audio
   @return new Audio
   */
  public static InstrumentAudio create(Instrument instrument, String name, String waveformKey, double start, double length, double tempo, double pitch, double density) {
    return create()
      .setInstrumentId(instrument.getId())
      .setName(name)
      .setWaveformKey(waveformKey)
      .setStart(start)
      .setLength(length)
      .setTempo(tempo)
      .setPitch(pitch)
      .setDensity(density);
  }

  /**
   Get id of Instrument to which this entity belongs

   @return instrument id
   */
  public UUID getInstrumentId() {
    return instrumentId;
  }

  /**
   Set id of Instrument to which this entity belongs

   @param instrumentId to which this entity belongs
   @return this Instrument Entity (for chaining setters)
   */
  public InstrumentAudio setInstrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public UUID getParentId() {
    return instrumentId;
  }

  /**
   Get Density

   @return Density
   */
  public Double getDensity() {
    return density;
  }

  /**
   Get Length

   @return Length
   */
  public Double getLength() {
    return length;
  }

  /**
   Get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return RESOURCE_HAS_MANY;
  }

  /**
   Get Pitch

   @return Pitch
   */
  public Double getPitch() {
    return pitch;
  }

  /**
   Get Start

   @return Start
   */
  public Double getStart() {
    return start;
  }

  /**
   Get Tempo

   @return Tempo
   */
  public Double getTempo() {
    return tempo;
  }

  /**
   Get WaveformKey

   @return WaveformKey
   */
  public String getWaveformKey() {
    return waveformKey;
  }

  /**
   Set Length

   @param length to set
   @return this Audio (for chaining setters)
   */
  public InstrumentAudio setLength(Double length) {
    this.length = length;
    return this;
  }

  /**
   Set Density

   @param density to set
   @return this Audio (for chaining setters)
   */
  public InstrumentAudio setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   Set Name

   @param name to set
   @return this Audio (for chaining setters)
   */
  public InstrumentAudio setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set Start

   @param start to set
   @return this Audio (for chaining setters)
   */
  public InstrumentAudio setStart(Double start) {
    this.start = start;
    return this;
  }

  /**
   Set Tempo

   @param tempo to set
   @return this Audio (for chaining setters)
   */
  public InstrumentAudio setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   Set Pitch

   @param pitch to set
   @return this Audio (for chaining setters)
   */
  public InstrumentAudio setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  /**
   Set WaveformKey

   @param waveformKey to set
   @return this Audio (for chaining setters)
   */
  public InstrumentAudio setWaveformKey(String waveformKey) {
    if (null != waveformKey) {
      this.waveformKey = waveformKey.trim();
    }
    return this;
  }

  @Override
  public void validate() throws CoreException {
    require(instrumentId, "Instrument ID");

    if (Objects.isNull(name) || name.isEmpty())
      throw new CoreException("Name is required.");

    if (Objects.isNull(waveformKey) || waveformKey.isEmpty())
      waveformKey = "";

    if (Objects.isNull(density))
      density = 0.5d;

    if (Objects.isNull(start))
      start = 0.0d;

    if (Objects.isNull(length))
      length = 0.0d;

    require(tempo, "Tempo");
    require(pitch, "Root Pitch");
  }
}
