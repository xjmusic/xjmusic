//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Meme;
import io.xj.core.model.segment.impl.SegmentSubEntity;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentMeme extends SegmentSubEntity implements Meme {
  private String name;

  /**
   Get segment meme of specified name

   @param name of meme
   @return new segment meme
   */
  public static SegmentMeme of(String name) {
    return new SegmentMeme().setName(name);
  }

  /**
   Get name of meme

   @return name
   */
  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .build();
  }

  /**
   set Name

   @param name to set
   @return this SegmentMeme (for chaining methods)
   */
  public SegmentMeme setName(String name) {
    this.name = name;
    return this;
  }

  /**
   set SegmentId

   @param segmentId to which this entity belongs
   @return this SegmentMeme (for chaining methods)
   */
  public SegmentMeme setSegmentId(BigInteger segmentId) {
    super.setSegmentId(segmentId);
    return this;
  }

  @Override
  public SegmentMeme setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public SegmentMeme validate() throws CoreException {
    super.validate();
    require(name, "Name");
    return this;
  }
}
