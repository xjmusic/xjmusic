// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.program.sub.Event;
import io.xj.core.model.segment.impl.SegmentSubEntity;

import java.math.BigInteger;
import java.util.UUID;

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
public class Pick extends SegmentSubEntity {
  public static final Double LENGTH_MINIMUM = 0.01;
  public static final Double AMPLITUDE_MINIMUM = 0.0;
  public static final Double PITCH_MINIMUM = 1.0;
  private UUID arrangementId;
  private UUID audioId;
  private UUID patternEventId;
  private Double start;
  private Double length;
  private Double amplitude;
  private Double pitch;
  private String name;
  private UUID voiceId;

  /**
   get Amplitude

   @return getAmplitude
   */
  public Double getAmplitude() {
    return amplitude;
  }

  /**
   get ArrangementId

   @return getArrangementId
   */
  public UUID getArrangementId() {
    return arrangementId;
  }

  /**
   get AudioId

   @return getAudioId
   */
  public UUID getAudioId() {
    return audioId;
  }

  /**
   get Name

   @return getName
   */
  public String getName() {
    return name;
  }

  /**
   Length from Start position, in Seconds

   @return seconds
   */
  public Double getLength() {
    return length;
  }

  @Override
  public BigInteger getParentId() {
    return getSegmentId();
  }

  /**
   get PatternEventId

   @return getPatternEventId
   */
  public UUID getPatternEventId() {
    return patternEventId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("start")
      .add("length")
      .add("amplitude")
      .add("pitch")
      .add("name")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Arrangement.class)
      .add(Audio.class)
      .add(Event.class)
      .build();
  }

  /**
   get Pitch

   @return getPitch
   */
  public Double getPitch() {
    return pitch;
  }

  /**
   Start position from beginning of segment, in Seconds

   @return seconds
   */
  public Double getStart() {
    return start;
  }

  /**
   get VoiceId

   @return getVoiceId
   */
  public UUID getVoiceId() {
    return voiceId;
  }

  /**
   set Amplitude

   @param amplitude to set
   @return this Pick (for chaining methods)
   */
  public Pick setAmplitude(Double amplitude) {
    this.amplitude = amplitude;
    return this;
  }

  /**
   set ArrangementId

   @param arrangementId to set
   @return this Pick (for chaining methods)
   */
  public Pick setArrangementId(UUID arrangementId) {
    this.arrangementId = arrangementId;
    return this;
  }

  /**
   set AudioId

   @param audioId to set
   @return this Pick (for chaining methods)
   */
  public Pick setAudioId(UUID audioId) {
    this.audioId = audioId;
    return this;
  }

  /**
   set Name

   @param name to set
   @return this Pick (for chaining methods)
   */
  public Pick setName(String name) {
    this.name = name;
    return this;
  }

  /**
   set Length

   @param length to set
   @return this Pick (for chaining methods)
   */
  public Pick setLength(Double length) {
    this.length = length;
    return this;
  }

  /**
   set PatternEventId

   @param patternEventId to set
   @return this Pick (for chaining methods)
   */
  public Pick setPatternEventId(UUID patternEventId) {
    this.patternEventId = patternEventId;
    return this;
  }

  /**
   set Pitch

   @param pitch to set
   @return this Pick (for chaining methods)
   */
  public Pick setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  /**
   set Start

   @param start to set
   @return this Pick (for chaining methods)
   */
  public Pick setStart(Double start) {
    this.start = start;
    return this;
  }

  @Override
  public Pick setId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   set VoiceId

   @param voiceId to set
   @return this Pick (for chaining methods)
   */
  public Pick setVoiceId(UUID voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  @Override
  public Pick validate() throws CoreException {
    super.validate();

    require(arrangementId, "Arrangement ID");

    require(patternEventId, "Pattern EventEntity ID");

    require(audioId, "Audio ID");

    require(voiceId, "Voice ID");

    require(start, "Start");

    require(length, "Length");

    if (LENGTH_MINIMUM > length)
      throw new CoreException(String.format("Length must be at least %f", LENGTH_MINIMUM));

    require(amplitude, "Amplitude");

    if (AMPLITUDE_MINIMUM > amplitude)
      throw new CoreException(String.format("Amplitude must be at least %f", AMPLITUDE_MINIMUM));

    require(pitch, "Pitch");

    if (PITCH_MINIMUM > pitch)
      throw new CoreException(String.format("Pitch must be at least %f", PITCH_MINIMUM));

    return this;
  }
}
