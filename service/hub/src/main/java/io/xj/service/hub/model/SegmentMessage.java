// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.MessageEntity;
import io.xj.service.hub.entity.MessageType;

import java.util.UUID;

/**
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentMessage extends MessageEntity {


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
    } catch (ValueException e) {
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
  public void validate() throws ValueException {
    super.validate();

    Value.require(segmentId, "Segment ID");

    Value.requireNo(typeException, "Type");
    Value.require(type, "Type");

    MessageEntity.validate(this);
  }

}
