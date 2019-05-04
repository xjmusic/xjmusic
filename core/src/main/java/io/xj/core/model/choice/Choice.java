// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import com.google.common.collect.Lists;
import io.xj.core.exception.CoreException;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentEntity;
import io.xj.core.model.sequence.SequenceType;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class Choice extends SegmentEntity {
  private BigInteger sequenceId;
  private SequenceType type;
  private Integer transpose;
  private BigInteger sequencePatternId;

  public static Collection<Choice> aggregate(Collection<Segment> segments) {
    Collection<Choice> aggregate = Lists.newArrayList();
    segments.forEach(segment -> aggregate.addAll(segment.getChoices()));
    return aggregate;
  }

  @Override
  public Choice setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public Choice setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  public BigInteger getSequenceId() {
    return sequenceId;
  }

  public Choice setSequenceId(BigInteger value) {
    sequenceId = value;
    return this;
  }

  public BigInteger getSequencePatternId() {
    return sequencePatternId;
  }

  public Choice setSequencePatternId(BigInteger value) {
    sequencePatternId = value;
    return this;
  }

  public SequenceType getType() {
    return type;
  }

  public Choice setType(String value) {
    type = SequenceType.valueOf(value);
    return this;
  }

  public Choice setTypeEnum(SequenceType type) {
    this.type = type;
    return this;
  }

  public Integer getTranspose() {
    return transpose;
  }

  public Choice setTranspose(Integer value) {
    transpose = value;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();

    if (Objects.nonNull(sequenceId) && Objects.nonNull(sequencePatternId))
      throw new CoreException("Cannot have both Sequence ID and Sequence Pattern ID.");

    if (Objects.isNull(sequenceId) && Objects.isNull(sequencePatternId))
      throw new CoreException("Required to have either Sequence ID or Sequence Pattern ID.");

    if (Objects.isNull(type))
      throw new CoreException("Type is required.");

    if (Objects.isNull(transpose))
      transpose = 0;
  }
}
