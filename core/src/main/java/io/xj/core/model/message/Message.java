// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

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
public abstract class Message extends Entity {

  private String _type;
  private MessageType type;

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = MessageType.validate(_type);

    if (Objects.isNull(type)) {
      throw new BusinessException("Type is required.");
    }
  }

  public static final String KEY_ONE = "message";
  public static final String KEY_MANY = "messages";

  public String getBody() {
    return body;
  }

  public Message setBody(String body) {
    this.body = body;
    return this;
  }

  public Message setType(String type) {
    _type = type;
    return this;
  }

  protected String body;

  public MessageType getType() {
    return type;
  }

  public Message setTypeEnum(MessageType type) {
    this.type = type;
    return this;
  }
}
