// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.entity.MemeEntity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentMeme extends MemeEntity {


  private UUID segmentId;

  /**
   Get segment meme of specified name

   @param name of meme
   @return new segment meme
   */
  public static SegmentMeme create(String name) {
    return create().setName(name);
  }

  /**
   of Segment MemeEntity

   @param segment to of meme in
   @param name    of MemeEntity
   @return new Segment MemeEntity
   */
  public static SegmentMeme create(Segment segment, String name) {
    return create()
      .setSegmentId(segment.getId())
      .setName(name);
  }

  /**
   of Segment MemeEntity

   @return new Segment MemeEntity
   */
  public static SegmentMeme create() {
    return new SegmentMeme().setId(UUID.randomUUID());
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
  public SegmentMeme setSegmentId(UUID segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  /**
   set Name

   @param name to set
   @return this SegmentMeme (for chaining methods)
   */
  public SegmentMeme setName(String name) {
    super.setName(name);
    return this;
  }

  @Override
  public SegmentMeme setId(UUID id) {
    super.setId(id);
    return this;
  }

  @Override
  public void validate() throws ValueException {
    super.validate();

    Value.require(segmentId, "Segment ID");

    MemeEntity.validate(this);
  }
}
