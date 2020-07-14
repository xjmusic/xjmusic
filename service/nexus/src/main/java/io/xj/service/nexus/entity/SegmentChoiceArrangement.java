// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.ProgramVoice;

import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class SegmentChoiceArrangement extends Entity {


  private UUID segmentChoiceId;
  private UUID programVoiceId;
  private UUID instrumentId;
  private UUID segmentId;

  /**
   of SegmentChoiceArrangement

   @return new SegmentChoiceArrangement
   */
  public static SegmentChoiceArrangement create(SegmentChoice choice) {
    return create()
      .setSegmentId(choice.getSegmentId())
      .setSegmentChoiceId(choice.getId());
  }

  /**
   of SegmentChoiceArrangement

   @param segment of SegmentChoiceArrangement
   @return new SegmentChoiceArrangement
   */
  public static SegmentChoiceArrangement create(Segment segment) {
    return create()
      .setSegmentId(segment.getId());
  }

  /**
   of SegmentChoiceArrangement

   @return new SegmentChoiceArrangement
   */
  public static SegmentChoiceArrangement create(SegmentChoice choice, ProgramVoice voice, Instrument instrument) {
    return create(choice)
      .setSegmentChoiceId(choice.getId())
      .setProgramVoiceId(voice.getId())
      .setInstrumentId(instrument.getId());
  }

  /**
   of SegmentChoiceArrangement

   @return new SegmentChoiceArrangement
   */
  public static SegmentChoiceArrangement create() {
    return new SegmentChoiceArrangement().setId(UUID.randomUUID());
  }

  /**
   of SegmentChoiceArrangement

   @param segment       of arrangement
   @param segmentChoice of arrangement
   @param programVoice  of arrangement
   @param instrument    of arrangement
   @return new SegmentChoiceArrangement
   */
  public static SegmentChoiceArrangement create(Segment segment, SegmentChoice segmentChoice, ProgramVoice programVoice, Instrument instrument) {
    return create(segment)
      .setSegmentChoiceId(segmentChoice.getId())
      .setProgramVoiceId(programVoice.getId())
      .setInstrumentId(instrument.getId());
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
  public SegmentChoiceArrangement setSegmentId(UUID segmentId) {
    this.segmentId = segmentId;
    return this;
  }


  /**
   get ChoiceId

   @return ChoiceId
   */
  public UUID getSegmentChoiceId() {
    return segmentChoiceId;
  }

  /**
   get InstrumentId

   @return InstrumentId
   */
  public UUID getInstrumentId() {
    return instrumentId;
  }

  /**
   get Voice ID

   @return Voice ID
   */
  public UUID getProgramVoiceId() {
    return programVoiceId;
  }

  /**
   set Choice ID

   @param segmentChoiceId to set
   @return this Arrangement (for chaining methods)
   */
  public SegmentChoiceArrangement setSegmentChoiceId(UUID segmentChoiceId) {
    this.segmentChoiceId = segmentChoiceId;
    return this;
  }

  /**
   set InstrumentId

   @param instrumentId to set
   @return this Arrangement (for chaining methods)
   */
  public SegmentChoiceArrangement setInstrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public SegmentChoiceArrangement setId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   set VoiceId

   @param programVoiceId to set
   @return this Arrangement (for chaining methods)
   */
  public SegmentChoiceArrangement setProgramVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
    return this;
  }

  @Override
  public void validate() throws ValueException {
    super.validate();

    Value.require(segmentId, "Segment ID");
    Value.require(segmentChoiceId, "Choice ID");
    Value.require(programVoiceId, "Voice ID");
    Value.require(instrumentId, "Instrument ID");
  }

}
