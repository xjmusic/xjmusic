// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_meme;

import com.google.common.collect.Lists;
import io.xj.core.exception.CoreException;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentEntity;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentMeme extends SegmentEntity implements Meme {
  private String name;

  public static SegmentMeme of(String name) {
    return new SegmentMeme().setName(name);
  }

  public static Collection<SegmentMeme> aggregate(Collection<Segment> segments) {
    Collection<SegmentMeme> aggregate = Lists.newArrayList();
    segments.forEach(segment -> aggregate.addAll(segment.getMemes()));
    return aggregate;
  }

  public SegmentMeme setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public SegmentMeme setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public SegmentMeme setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();
    if (Objects.isNull(name) || name.isEmpty()) {
      throw new CoreException("Name is required.");
    }
  }

  public String getName() {
    return name;
  }
}
