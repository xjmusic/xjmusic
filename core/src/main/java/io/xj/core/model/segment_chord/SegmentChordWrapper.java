// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

import java.util.Objects;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SegmentChordWrapper extends EntityWrapper {

  // SegmentChord
  private SegmentChord segmentChord;

  public SegmentChord getSegmentChord() {
    return segmentChord;
  }

  public SegmentChordWrapper setSegmentChord(SegmentChord segmentChord) {
    this.segmentChord = segmentChord;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public SegmentChord validate() throws BusinessException {
    if (Objects.isNull(segmentChord)) {
      throw new BusinessException("segmentChord is required.");
    }
    segmentChord.validate();
    return segmentChord;
  }

}
