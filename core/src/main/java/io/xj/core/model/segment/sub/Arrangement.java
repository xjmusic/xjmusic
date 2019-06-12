// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.program.sub.Voice;
import io.xj.core.model.segment.impl.SegmentSubEntity;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Arrangement extends SegmentSubEntity {
  private UUID choiceId;
  private UUID voiceId;
  private BigInteger instrumentId;

  /**
   get ChoiceId

   @return ChoiceId
   */
  public UUID getChoiceId() {
    return choiceId;
  }

  /**
   get InstrumentId

   @return InstrumentId
   */
  public BigInteger getInstrumentId() {
    return instrumentId;
  }

  @Override
  public BigInteger getParentId() {
    return getSegmentId();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Choice.class)
      .add(Voice.class)
      .add(Instrument.class)
      .build();
  }

  /**
   get Voice ID

   @return Voice ID
   */
  public UUID getVoiceId() {
    return voiceId;
  }

  /**
   set Choice ID

   @param choiceId to set
   @return this Arrangement (for chaining methods)
   */
  public Arrangement setChoiceId(UUID choiceId) {
    this.choiceId = choiceId;
    return this;
  }

  /**
   set InstrumentId

   @param instrumentId to set
   @return this Arrangement (for chaining methods)
   */
  public Arrangement setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public Arrangement setId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   set VoiceId

   @param voiceId to set
   @return this Arrangement (for chaining methods)
   */
  public Arrangement setVoiceId(UUID voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  @Override
  public Arrangement validate() throws CoreException {
    super.validate();
    require(choiceId, "Choice ID");
    require(voiceId, "Voice ID");
    require(instrumentId, "Instrument ID");
    return this;
  }

}
