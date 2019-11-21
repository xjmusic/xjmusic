//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.MessageEntity;
import io.xj.core.entity.MessageType;
import io.xj.core.exception.CoreException;

import java.util.UUID;

/**
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentMessage extends MessageEntity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(MessageEntity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Segment.class)
    .build();
  protected String body;
  private MessageType type;
  private Exception typeException;
  private UUID segmentId;

  /**
   of Segment Message

   @param segment in which to of message
   @param type    of Message
   @param body    of Message
   @return new Segment Message
   */
  public static SegmentMessage create(Segment segment, MessageType type, String body) {
    return create()
      .setSegmentId(segment.getId())
      .setTypeEnum(type)
      .setBody(body);
  }

  /**
   of Segment Message

   @return new Segment Message
   */
  public static SegmentMessage create() {
    return new SegmentMessage().setId(UUID.randomUUID());
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
  public SegmentMessage setSegmentId(UUID segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  @Override
  public String getBody() {
    return body;
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
  public MessageType getType() {
    return type;
  }

  @Override
  public SegmentMessage setBody(String body) {
    this.body = body;
    return this;
  }

  @Override
  public SegmentMessage setType(String type) {
    try {
      this.type = MessageType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  @Override
  public SegmentMessage setTypeEnum(MessageType type) {
    this.type = type;
    return this;
  }

  @Override
  public SegmentMessage setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();

    require(segmentId, "Segment ID");

    requireNo(typeException, "Type");
    require(type, "Type");

    MessageEntity.validate(this);
  }

}
