// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import java.util.Objects;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public abstract class Message extends Entity {

  public void validate() throws BusinessException {
    if (Objects.isNull(type)) {
      throw new BusinessException("Type is required.");
    }

    if (!MessageType.stringValues().contains(type)) {
      throw new BusinessException("Invalid type!");
    }
  }

  public static final String KEY_ONE = "message";
  public static final String KEY_MANY = "messages";

  protected String type;

  public String getBody() {
    return body;
  }

  public Message setBody(String body) {
    this.body = body;
    return this;
  }

  public Message setType(String type) {
    this.type = type;
    return this;
  }

  protected String body;

  public String getType() {
    return type;
  }

}
