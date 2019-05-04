// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.message;

import io.xj.core.exception.CoreException;

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
public interface Message {
  String KEY_ONE = "message";
  String KEY_MANY = "messages";

  void validate() throws CoreException;

  String getBody();

  Message setBody(String body);

  Message setType(String type) throws CoreException;

  MessageType getType();

  Message setTypeEnum(MessageType type);
}
