// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SegmentMemeWrapper extends EntityWrapper {

  // Segment
  private SegmentMeme segmentMeme;

  public SegmentMeme getSegmentMeme() {
    return segmentMeme;
  }

  public SegmentMemeWrapper setSegmentMeme(SegmentMeme segmentMeme) {
    this.segmentMeme = segmentMeme;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public SegmentMeme validate() throws BusinessException {
    if (this.segmentMeme == null) {
      throw new BusinessException("Segment is required.");
    }
    this.segmentMeme.validate();
    return this.segmentMeme;
  }

}
