// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.message;

import io.xj.core.exception.CoreException;

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
public interface Message {
  int BODY_LENGTH_LIMIT = 65535;
  String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";

  /**
   Validate message attributes

   @param message to validate
   @throws CoreException if invalid
   */
  static void validate(Message message) throws CoreException {
    if (Objects.isNull(message.getType()))
      throw new CoreException("Type is required.");

    if (null == message.getBody() || message.getBody().isEmpty())
      throw new CoreException("Body is required.");

    if (BODY_LENGTH_LIMIT < message.getBody().length())
      message.setBody(message.getBody().substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX);
  }

  Message validate() throws CoreException;

  String getBody();

  Message setBody(String body);

  Message setType(String type);

  MessageType getType();

  Message setTypeEnum(MessageType type);
}



