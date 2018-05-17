// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SegmentWrapper extends EntityWrapper {

  // Segment
  private Segment segment;

  public Segment getSegment() {
    return segment;
  }

  public SegmentWrapper setSegment(Segment segment) {
    this.segment = segment;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Segment validate() throws BusinessException {
    if (this.segment == null) {
      throw new BusinessException("Segment is required.");
    }
    this.segment.validate();
    return this.segment;
  }

}
