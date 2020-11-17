// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity.common;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.MessageType;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

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
public abstract class MessageEntity {
  static int BODY_LENGTH_LIMIT = 65535;
  static String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";
  protected ValueException typeException;
  private String body;
  private MessageType type;

  /**
   Validate message attributes

   @param message to validate
   @throws ValueException if invalid
   */
  public static void validate(Object message) throws ValueException {
    try {
      Value.require(Entities.get(message, "type"), "Type");
      String body = String.valueOf(Entities.get(message, "body").orElseThrow());
      Value.require(body, "Body");
      if (BODY_LENGTH_LIMIT < body.length())
        Entities.set(message, "body", body.substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX);

    } catch (EntityException e) {
      throw new ValueException(e);
    }
  }

  /**
   Get message body

   @return body
   */
  public String getBody() {
    return body;
  }

  /**
   Set message body

   @param body to set
   @return this message (for chaining setters)
   */
  public MessageEntity setBody(String body) {
    this.body = body;
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Pattern (for chaining setters)
   */
  public MessageEntity setType(String type) {
    try {
      this.type = MessageType.validate(type);
    } catch (ValueException e) {
      this.typeException = e;
    }
    return this;
  }

  /**
   Message Type

   @return type
   */
  public MessageType getType() {
    return type;
  }

  /**
   Set Message type by enum

   @param type to set
   @return this message (for chaining setters)
   */
  public MessageEntity setType(MessageType type) {
    this.type = type;
    return this;
  }
}



