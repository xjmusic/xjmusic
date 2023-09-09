package io.xj.gui.models;

import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.SegmentUtils;

import java.util.Objects;

public class SegmentOnTimeline {
  private final Segment segment;
  private final boolean active;

  public SegmentOnTimeline(Segment segment) {
    this.segment = segment;
    active = false;
  }

  public SegmentOnTimeline(Segment segment, Long activeAtChainMicros) {
    this.segment = segment;
    active = SegmentUtils.isIntersecting(segment, activeAtChainMicros);
  }

  public Segment getSegment() {
    return segment;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isSameButUpdated(SegmentOnTimeline other) {
    if (!Objects.equals(this.getId(), other.getSegment().getId()))
      return false;

    // true if state has changed
    if (!Objects.equals(this.getSegment().getState(), other.getSegment().getState()))
      return true;

    // true if active has changed
    if (!Objects.equals(this.isActive(), other.isActive()))
      return true;

    // true if updated-at has changed
    return !Objects.equals(this.getSegment().getUpdatedAt(), other.getSegment().getUpdatedAt());
  }

  public int getId() {
    return segment.getId();
  }
}
