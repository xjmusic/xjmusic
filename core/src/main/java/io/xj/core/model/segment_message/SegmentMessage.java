// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.message.Message;

import java.math.BigInteger;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class SegmentMessage extends Message {
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "segmentMessage";
  public static final String KEY_MANY = "segmentMessages";

  private static final int BODY_LENGTH_LIMIT = 65535;
  private static final String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";

  protected BigInteger segmentId;

  @Override
  public BigInteger getParentId() {
    return segmentId;
  }

  @Override
  public void validate() throws BusinessException {
    super.validate();
    if (Objects.isNull(segmentId)) {
      throw new BusinessException("Segment ID is required.");
    }
    if (Objects.isNull(body) || body.isEmpty()) {
      throw new BusinessException("Body is required.");
    }
    if (BODY_LENGTH_LIMIT < body.length()) {
      body = body.substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX;
    }
  }

  @Override
  public SegmentMessage setBody(String body) {
    this.body = body;
    return this;
  }

  public BigInteger getSegmentId() {
    return segmentId;
  }

  public SegmentMessage setSegmentId(BigInteger segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  @Override
  public SegmentMessage setType(String type) {
    super.setType(type);
    return this;
  }

}
