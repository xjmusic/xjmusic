// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.entity;

import com.google.common.collect.ImmutableList;
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
public abstract class MessageEntity extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES =
    ImmutableList.<String>builder()
      .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
      .add("body")
      .add("type")
      .build();
  static int BODY_LENGTH_LIMIT = 65535;
  static String BODY_TRUNCATE_SUFFIX = " (truncated to fit character limit)";
  private String body;
  private MessageType type;
  protected CoreException typeException;

  /**
   Validate message attributes

   @param message to validate
   @throws CoreException if invalid
   */
  public static void validate(MessageEntity message) throws CoreException {
    require(message.getBody(), "Body");
    require(message.getType(), "Type");

    if (BODY_LENGTH_LIMIT < message.getBody().length())
      message.setBody(message.getBody().substring(0, BODY_LENGTH_LIMIT - BODY_TRUNCATE_SUFFIX.length()) + BODY_TRUNCATE_SUFFIX);
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
    } catch (CoreException e) {
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
  public MessageEntity setTypeEnum(MessageType type) {
    this.type = type;
    return this;
  }
}



