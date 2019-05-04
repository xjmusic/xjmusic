// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.arrangement;

import com.google.common.collect.Lists;
import io.xj.core.exception.CoreException;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentEntity;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Arrangement extends SegmentEntity {
  private UUID choiceUuid;
  private BigInteger voiceId;
  private BigInteger instrumentId;

  public static Collection<Arrangement> aggregate(Collection<Segment> segments) {
    Collection<Arrangement> aggregate = Lists.newArrayList();
    segments.forEach(segment -> aggregate.addAll(segment.getArrangements()));
    return aggregate;
  }

  public UUID getChoiceUuid() {
    return choiceUuid;
  }

  public Arrangement setChoiceUuid(UUID choiceUuid) {
    this.choiceUuid = choiceUuid;
    return this;
  }

  public BigInteger getVoiceId() {
    return voiceId;
  }

  public Arrangement setVoiceId(BigInteger voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  public BigInteger getInstrumentId() {
    return instrumentId;
  }

  public Arrangement setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public Arrangement setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();

    if (Objects.isNull(choiceUuid)) {
      throw new CoreException("Choice ID is required.");
    }
    if (Objects.isNull(voiceId)) {
      throw new CoreException("Voice ID is required.");
    }
    if (Objects.isNull(instrumentId)) {
      throw new CoreException("Instrument ID is required.");
    }
  }

}
