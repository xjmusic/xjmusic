// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.chord.Chord;

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
public class SegmentChord extends Chord {
  public static final String KEY_ONE = "segmentChord";
  public static final String KEY_MANY = "segmentChords";

  private BigInteger segmentId;

  @Override
  public SegmentChord setName(String name) {
    this.name = name;
    return this;
  }

  public BigInteger getSegmentId() {
    return segmentId;
  }

  public SegmentChord setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  @Override
  public SegmentChord setPosition(Double position) {
    this.position = roundPosition(position);
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return segmentId;
  }

  @Override
  public void validate() throws BusinessException {
    if (Objects.isNull(segmentId)) {
      throw new BusinessException("Segment ID is required.");
    }
    super.validate();
  }

  @Override
  public SegmentChord of(String name) {
    return new SegmentChord().setName(name);
  }


}
