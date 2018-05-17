// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

import java.util.Objects;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SegmentMessageWrapper extends EntityWrapper {

  // Choice
  private SegmentMessage segmentMessage;

  public SegmentMessage getSegmentMessage() {
    return segmentMessage;
  }

  public SegmentMessageWrapper setSegmentMessage(SegmentMessage segmentMessage) {
    this.segmentMessage = segmentMessage;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public SegmentMessage validate() throws BusinessException {
    if (Objects.isNull(segmentMessage)) {
      throw new BusinessException("Segment Message is required.");
    }
    segmentMessage.validate();
    return segmentMessage;
  }

}
