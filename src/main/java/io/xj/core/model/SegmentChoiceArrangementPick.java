// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

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
public class SegmentChoiceArrangementPick extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("start")
    .add("length")
    .add("amplitude")
    .add("pitch")
    .add("name")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Segment.class)
    .add(SegmentChoiceArrangement.class)
    .add(InstrumentAudio.class)
    .add(ProgramSequencePatternEvent.class)
    .build();
  public static final Double LENGTH_MINIMUM = 0.01;
  public static final Double AMPLITUDE_MINIMUM = 0.0;
  public static final Double PITCH_MINIMUM = 1.0;
  private UUID segmentChoiceArrangementId;
  private UUID instrumentAudioId;
  private UUID programSequencePatternEventId;
  private UUID segmentId;
  private Double start;
  private Double length;
  private Double amplitude;
  private Double pitch;
  private String name;

  /**
   of Pick

   @return new Pick
   */
  public static SegmentChoiceArrangementPick create() {
    return new SegmentChoiceArrangementPick().setId(UUID.randomUUID());
  }

  /**
   Create pick from segment arrange

   @param segmentChoiceArrangement to of pick from
   @return segment pick
   */
  public static SegmentChoiceArrangementPick create(SegmentChoiceArrangement segmentChoiceArrangement) {
    return create()
      .setSegmentId(segmentChoiceArrangement.getSegmentId())
      .setSegmentChoiceArrangementId(segmentChoiceArrangement.getId());
  }

  /**
   Create pick from segment arrange

   @param segmentChoiceArrangement to of pick from
   @return segment pick
   */
  public static SegmentChoiceArrangementPick create(
    SegmentChoiceArrangement segmentChoiceArrangement,
    ProgramSequencePatternEvent programSequencePatternEvent,
    InstrumentAudio instrumentAudio,
    Double start,
    Double length,
    Double amplitude,
    Double pitch,
    String name) {
    return create()
      .setSegmentId(segmentChoiceArrangement.getSegmentId())
      .setSegmentChoiceArrangementId(segmentChoiceArrangement.getId())
      .setProgramSequencePatternEventId(programSequencePatternEvent.getId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setStart(start)
      .setLength(length)
      .setAmplitude(amplitude)
      .setPitch(pitch)
      .setName(name);
  }

  @Override
  public UUID getParentId() {
    return segmentId;
  }

  /**
   Get id of Segment to which this entity belongs

   @return segment id
   */
  public UUID getSegmentId() {
    return segmentId;
  }

  /**
   Set id of Segment to which this entity belongs

   @param segmentId to which this entity belongs
   @return this Segment Entity (for chaining setters)
   */
  public SegmentChoiceArrangementPick setSegmentId(UUID segmentId) {
    this.segmentId = segmentId;
    return this;
  }


  /**
   get Amplitude

   @return getAmplitude
   */
  public Double getAmplitude() {
    return amplitude;
  }

  /**
   get ArrangementId

   @return getSegmentChoiceArrangementId
   */
  public UUID getSegmentChoiceArrangementId() {
    return segmentChoiceArrangementId;
  }

  /**
   get AudioId

   @return getInstrumentAudioId
   */
  public UUID getInstrumentAudioId() {
    return instrumentAudioId;
  }

  /**
   get Name

   @return getName
   */
  public String getName() {
    return name;
  }

  /**
   Length of Start position, in Seconds

   @return seconds
   */
  public Double getLength() {
    return length;
  }

  /**
   get EventId

   @return getProgramSequencePatternEventId
   */
  public UUID getProgramSequencePatternEventId() {
    return programSequencePatternEventId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  /**
   get Pitch

   @return getPitch
   */
  public Double getPitch() {
    return pitch;
  }

  /**
   Start position of beginning of segment, in Seconds

   @return seconds
   */
  public Double getStart() {
    return start;
  }

  /**
   set Amplitude

   @param amplitude to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setAmplitude(Double amplitude) {
    this.amplitude = amplitude;
    return this;
  }

  /**
   set ArrangementId

   @param segmentChoiceArrangementId to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setSegmentChoiceArrangementId(UUID segmentChoiceArrangementId) {
    this.segmentChoiceArrangementId = segmentChoiceArrangementId;
    return this;
  }

  /**
   set AudioId

   @param instrumentAudioId to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setInstrumentAudioId(UUID instrumentAudioId) {
    this.instrumentAudioId = instrumentAudioId;
    return this;
  }

  /**
   set Name

   @param name to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setName(String name) {
    this.name = name;
    return this;
  }

  /**
   set Length

   @param length to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setLength(Double length) {
    this.length = length;
    return this;
  }

  /**
   set EventId

   @param programSequencePatternEventId to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setProgramSequencePatternEventId(UUID programSequencePatternEventId) {
    this.programSequencePatternEventId = programSequencePatternEventId;
    return this;
  }

  /**
   set Pitch

   @param pitch to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  /**
   set Start

   @param start to set
   @return this Pick (for chaining methods)
   */
  public SegmentChoiceArrangementPick setStart(Double start) {
    this.start = start;
    return this;
  }

  @Override
  public SegmentChoiceArrangementPick setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();

    require(segmentId, "Segment ID");
    require(segmentChoiceArrangementId, "Arrangement ID");
    require(programSequencePatternEventId, "Pattern Event ID");
    require(instrumentAudioId, "Audio ID");

    require(start, "Start");

    require(length, "Length");
    requireMinimum(LENGTH_MINIMUM, length, "Length");

    require(amplitude, "Amplitude");
    requireMinimum(AMPLITUDE_MINIMUM, amplitude, "Amplitude");

    require(pitch, "Pitch");
    requireMinimum(PITCH_MINIMUM, pitch, "Pitch");
  }
}
