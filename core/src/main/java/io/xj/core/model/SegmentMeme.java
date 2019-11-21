//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.MemeEntity;
import io.xj.core.exception.CoreException;

import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 */
public class SegmentMeme extends MemeEntity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(MemeEntity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Segment.class)
    .build();
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

   @return new Segment MemeEntity
   @param segment to of meme in
   @param name    of MemeEntity
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
  public SegmentMeme setSegmentId(UUID segmentId) {
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
  public void validate() throws CoreException {
    super.validate();

    require(segmentId, "Segment ID");

    MemeEntity.validate(this);
  }
}
