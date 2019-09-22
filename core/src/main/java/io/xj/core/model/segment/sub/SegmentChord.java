//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.ChordEntity;
import io.xj.core.model.segment.impl.SegmentSubEntity;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentChord extends SegmentSubEntity implements ChordEntity<SegmentChord> {
  private String name;
  private Double position;

  /**
   Get Segment ChordEntity of a specified name

   @param name of chord
   @return new chord
   */
  public static SegmentChord of(String name) {
    return new SegmentChord().setName(name);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public BigInteger getParentId() {
    return getSegmentId();
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .add("position")
      .build();
  }

  @Override
  public Double getPosition() {
    return position;
  }

  @Override
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  @Override
  public Boolean isChord() {
    return !isNoChord();
  }

  @Override
  public SegmentChord setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public SegmentChord setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public SegmentChord setSegmentId(BigInteger segmentId) {
    super.setSegmentId(segmentId);
    return this;
  }

  @Override
  public SegmentChord setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override
  public io.xj.music.Chord toMusical() {
    return new io.xj.music.Chord(name);
  }

  @Override
  public String toString() {
    return String.format("%s@%s", name, position);
  }

  @Override
  public SegmentChord validate() throws CoreException {
    super.validate();
    ChordEntity.validate(this);
    return this;
  }

}
