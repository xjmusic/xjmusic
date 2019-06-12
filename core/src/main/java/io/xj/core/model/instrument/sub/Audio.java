//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.impl.InstrumentSubEntity;

import java.util.Objects;

/**
 [#166708597] Audio model handles all of its own entities
 */
public class Audio extends InstrumentSubEntity {
  private String waveformKey;
  private String name;
  private Double start;
  private Double length;
  private Double tempo;
  private Double pitch;
  private Double density;

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
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("waveformKey")
      .add("name")
      .add("start")
      .add("length")
      .add("tempo")
      .add("pitch")
      .add("density")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(AudioChord.class)
      .add(AudioEvent.class)
      .build();
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
  public Audio setLength(Double length) {
    this.length = length;
    return this;
  }

  /**
   Set Density

   @param density to set
   @return this Audio (for chaining setters)
   */
  public Audio setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   Set Name

   @param name to set
   @return this Audio (for chaining setters)
   */
  public Audio setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set Start

   @param start to set
   @return this Audio (for chaining setters)
   */
  public Audio setStart(Double start) {
    this.start = start;
    return this;
  }

  /**
   Set Tempo

   @param tempo to set
   @return this Audio (for chaining setters)
   */
  public Audio setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   Set Pitch

   @param pitch to set
   @return this Audio (for chaining setters)
   */
  public Audio setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  /**
   Set WaveformKey

   @param waveformKey to set
   @return this Audio (for chaining setters)
   */
  public Audio setWaveformKey(String waveformKey) {
    if (null != waveformKey) {
      this.waveformKey = waveformKey.trim();
    }
    return this;
  }

  @Override
  public Audio validate() throws CoreException {
    super.validate();

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

    return this;
  }
}
