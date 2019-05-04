//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment;

import io.xj.core.exception.CoreException;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public abstract class SegmentEntity {
  protected BigInteger segmentId;
  protected UUID uuid;

  /**
   Get entity uuid

   @return entity uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   Set entity uuid

   @return this segment entity (for chaining setters)
   */
  public abstract SegmentEntity setUuid(UUID uuid);

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
  public SegmentEntity setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  /**
   Validate this Segment entity

   @throws CoreException if invalid
   */
  public void validate() throws CoreException {
    if (Objects.isNull(segmentId))
      throw new CoreException("Segment ID is required.");
  }
}
