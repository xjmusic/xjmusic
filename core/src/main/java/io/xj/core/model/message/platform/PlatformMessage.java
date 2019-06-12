// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.message.platform;

import com.google.common.collect.ImmutableList;
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
  public static final String RESOURCE_TYPE = "platform-messages";
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.of("body", "type");
  protected String body;
  private MessageType type;
  private CoreException typeException;

  /**
   get Body

   @return Body
   */
  public String getBody() {
    return body;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .addAll(RESOURCE_ATTRIBUTE_NAMES)
      .build();
  }

  @Override
  public String getResourceType() {
    return RESOURCE_TYPE;
  }

  /**
   get Type

   @return Type
   */
  public MessageType getType() {
    return type;
  }

  @Override
  public BigInteger getParentId() {
    return new BigInteger(""); // no parent
  }

  @Override
  public PlatformMessage setBody(String body) {
    this.body = body;
    return this;
  }

  @Override
  public PlatformMessage setType(String type) {
    try {
      this.type = MessageType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }

    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Message (for chaining methods)
   */
  public Message setTypeEnum(MessageType type) {
    this.type = type;
    return this;
  }

  @Override
  public PlatformMessage validate() throws CoreException {
    if (Objects.nonNull(typeException))
      throw new CoreException("Invalid type value.", typeException);

    Message.validate(this);

    return this;
  }
}
