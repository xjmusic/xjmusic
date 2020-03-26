// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.ChordEntity;

import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentChord extends ChordEntity {


  private UUID segmentId;

  /**
   Get Segment ChordEntity of a specified name

   @param name of chord
   @return new chord
   */
  public static SegmentChord of(String name) {
    return new SegmentChord().setName(name);
  }

  /**
   of Segment ChordEntity

   @param segment  to of chord in
   @param position of ChordEntity
   @param name     of ChordEntity
   @return new Segment ChordEntity
   */
  public static SegmentChord create(Segment segment, double position, String name) {
    return create()
      .setSegmentId(segment.getId())
      .setPosition(position)
      .setName(name);
  }

  /**
   of Segment ChordEntity

   @return new Segment ChordEntity
   */
  public static SegmentChord create() {
    return new SegmentChord().setId(UUID.randomUUID());
  }

  @Override
  public UUID getParentId() {
    return segmentId;
  }

  /**
   Get id of Segment to which this entity belongs

   @return segment id
   */
  public UUID getSegmentId() {
    return segmentId;
  }

  /**
   Set id of Segment to which this entity belongs

   @param segmentId to which this entity belongs
   @return this Segment Entity (for chaining setters)
   */
  public SegmentChord setSegmentId(UUID segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  @Override
  public SegmentChord setName(String name) {
    super.setName(name);
    return this;
  }

  @Override
  public SegmentChord setId(UUID id) {
    super.setId(id);
    return this;
  }

  @Override
  public SegmentChord setPosition(Double position) {
    super.setPosition(position);
    return this;
  }

  @Override
  public void validate() throws ValueException {
    super.validate();

    Value.require(segmentId, "Segment ID");

    ChordEntity.validate(this);
  }

}
