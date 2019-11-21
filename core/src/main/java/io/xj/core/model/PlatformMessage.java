// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.MessageEntity;
import io.xj.core.entity.MessageType;
import io.xj.core.exception.CoreException;

import java.time.Instant;
import java.util.UUID;

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
public class PlatformMessage extends MessageEntity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES =
    ImmutableList.<String>builder()
      .addAll(MessageEntity.RESOURCE_ATTRIBUTE_NAMES)
      .build();

  /**
   Create a new Platform Message

   @param type of PlatformMessage
   @param body of PlatformMessage
   @param at   created/updated  of PlatformMessage
   @return PlatformMessage
   */
  public static PlatformMessage create(MessageType type, String body, Instant at) {
    return (PlatformMessage) PlatformMessage.create()
      .setTypeEnum(type)
      .setBody(body)
      .setCreatedAtInstant(at)
      .setUpdatedAtInstant(at);
  }

  /**
   Creaet a new Platform Message

   @return new Platform Message
   */
  public static PlatformMessage create() {
    return (PlatformMessage) new PlatformMessage().setId(UUID.randomUUID());
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }


  @Override
  public PlatformMessage setBody(String body) {
    super.setBody(body);
    return this;
  }

  @Override
  public PlatformMessage setType(String type) {
    super.setType(type);
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Message (for chaining methods)
   */
  public MessageEntity setTypeEnum(MessageType type) {
    super.setTypeEnum(type);
    return this;
  }

  @Override
  public void validate() throws CoreException {
    requireNo(typeException, "Type");

    MessageEntity.validate(this);
  }
}
