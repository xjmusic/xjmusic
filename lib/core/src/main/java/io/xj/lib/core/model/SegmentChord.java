// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.ChordEntity;
import io.xj.lib.core.exception.CoreException;

import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentChord extends ChordEntity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(ChordEntity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Segment.class)
    .build();
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

   @return new Segment ChordEntity
   @param segment  to of chord in
   @param position of ChordEntity
   @param name     of ChordEntity
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
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
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
  public void validate() throws CoreException {
    super.validate();

    require(segmentId, "Segment ID");

    ChordEntity.validate(this);
  }

}
