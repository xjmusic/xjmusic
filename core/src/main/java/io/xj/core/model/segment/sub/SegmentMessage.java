//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.message.Message;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.segment.impl.SegmentSubEntity;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentMessage extends SegmentSubEntity implements Message {
  protected String body;
  private MessageType type;
  private Exception typeException;

  @Override
  public String getBody() {
    return body;
  }

  /**
   Get Parent (segment) ID

   @return parent segment ID
   */
  public BigInteger getParentId() {
    return getSegmentId();
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("type")
      .add("body")
      .build();
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

  /**
   Set parent (segment) ID

   @param segmentId to which this entity belongs
   @return this SegmentMessage (for chaining methods)
   */
  public SegmentMessage setSegmentId(BigInteger segmentId) {
    super.setSegmentId(segmentId);
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
  public SegmentMessage validate() throws CoreException {
    super.validate();
    requireNo(typeException, "Type");
    require(type, "Type");
    Message.validate(this);
    return this;
  }

}
