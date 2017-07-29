// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.message;

import io.xj.core.model.Entity;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.util.Text;

import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.List;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
abstract public class Message extends Entity {

  /**
   It is implied that choice types must equal idea types
   */
  public final static List<String> TYPES = MessageType.stringValues();

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "message";
  public static final String KEY_MANY = "messages";

  protected ULong linkId;
  protected String type;

  public String getBody() {
    return body;
  }

  public Message setBody(String body) {
    this.body = body;
    return this;
  }

  public Message setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  public Message setType(String type) {
    this.type = Text.LowerSlug(type);
    return this;
  }

  protected String body;

  public ULong getLinkId() {
    return linkId;
  }

  public MessageType getType() {
    return MessageType.valueOf(type);
  }

  public abstract LinkMessage setType(MessageType type);
}
