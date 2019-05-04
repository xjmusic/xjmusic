// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.platform_message;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.message.Message;
import io.xj.core.model.message.MessageType;

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
public class PlatformMessage extends EntityImpl implements Message {
  public static final String KEY_ONE = "platformMessage";
  public static final String KEY_MANY = "platformMessages";
  private static final int BODY_LENGTH_LIMIT = 65535;
  private static final String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";
  protected String body;
  private MessageType type;

  @Override
  public BigInteger getParentId() {
    return new BigInteger(""); // no parent
  }

  @Override
  public void validate() throws CoreException {
    if (Objects.isNull(type)) {
      throw new CoreException("Type is required.");
    }
    if (null == body || body.isEmpty()) {
      throw new CoreException("Body is required.");
    }
    if (BODY_LENGTH_LIMIT < body.length()) {
      body = body.substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX;
    }
  }

  @Override
  public PlatformMessage setBody(String body) {
    this.body = body;
    return this;
  }

  @Override
  public PlatformMessage setType(String type) {
    this.type = MessageType.valueOf(type);
    return this;
  }

  public String getBody() {
    return body;
  }

  public MessageType getType() {
    return type;
  }

  public Message setTypeEnum(MessageType type) {
    this.type = type;
    return this;
  }
}
