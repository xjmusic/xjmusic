//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment.impl;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.SubEntityImpl;
import io.xj.core.model.segment.Segment;

import java.math.BigInteger;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public abstract class SegmentSubEntity extends SubEntityImpl {
  private BigInteger segmentId;

  @Override
  public BigInteger getParentId() {
    return segmentId;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Segment.class)
      .build();
  }

  /**
   Get id of Segment to which this entity belongs

   @return segment id
   */
  public BigInteger getSegmentId() {
    return segmentId;
  }

  /**
   Set id of Segment to which this entity belongs

   @param segmentId to which this entity belongs
   @return this Segment Entity (for chaining setters)
   */
  public SegmentSubEntity setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  /**
   Validate this Segment entity

   @throws CoreException if invalid
   */
  public SegmentSubEntity validate() throws CoreException {
    require(segmentId, "Segment ID");
    return this;
  }
}
