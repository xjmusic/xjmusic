// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment_message;

import com.google.common.collect.Lists;
import io.xj.core.exception.CoreException;
import io.xj.core.model.message.Message;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentEntity;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentMessage extends SegmentEntity implements Message {
  public static final String KEY_ONE = "segmentMessage";
  public static final String KEY_MANY = "segmentMessages";
  private static final int BODY_LENGTH_LIMIT = 65535;
  private static final String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";
  protected String body;
  private MessageType type;

  public static Collection<SegmentMessage> aggregate(Collection<Segment> segments) {
    Collection<SegmentMessage> aggregate = Lists.newArrayList();
    segments.forEach(segment -> aggregate.addAll(segment.getMessages()));
    return aggregate;
  }

  public BigInteger getParentId() {
    return segmentId;
  }

  @Override
  public void validate() throws CoreException {
    if (Objects.isNull(type)) {
      throw new CoreException("Type is required.");
    }
    if (Objects.isNull(segmentId)) {
      throw new CoreException("Segment ID is required.");
    }
    if (Objects.isNull(body) || body.isEmpty()) {
      throw new CoreException("Body is required.");
    }
    if (BODY_LENGTH_LIMIT < body.length()) {
      body = body.substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX;
    }
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public SegmentMessage setBody(String body) {
    this.body = body;
    return this;
  }

  @Override
  public SegmentMessage setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public SegmentMessage setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  @Override
  public SegmentMessage setType(String type) throws CoreException {
    this.type = MessageType.validate(type);
    return this;
  }

  @Override
  public MessageType getType() {
    return type;
  }

  @Override
  public SegmentMessage setTypeEnum(MessageType type) {
    this.type = type;
    return this;
  }

}
