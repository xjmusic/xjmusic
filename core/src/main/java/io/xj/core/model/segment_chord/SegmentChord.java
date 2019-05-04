// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_chord;

import com.google.common.collect.Lists;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentEntity;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.util.Collection;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentChord extends SegmentEntity implements Chord<SegmentChord> {
  private String name;
  private Double position;

  public static Collection<SegmentChord> aggregate(Collection<Segment> segments) {
    Collection<SegmentChord> aggregate = Lists.newArrayList();
    segments.forEach(segment -> aggregate.addAll(segment.getChords()));
    return aggregate;
  }

  public static SegmentChord of(String name) {
    return new SegmentChord().setName(name);
  }

  @Override
  public SegmentChord setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public SegmentChord setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public SegmentChord setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  @Override
  public SegmentChord setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();
    Chord.validate(this);
  }

  @Override
  public String getName() {
    return name;
  }


  @Override
  public Double getPosition() {
    return position;
  }

  @Override
  public io.xj.music.Chord toMusical() {
    return new io.xj.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

  @Override
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  @Override
  public Boolean isChord() {
    return !isNoChord();
  }

}
