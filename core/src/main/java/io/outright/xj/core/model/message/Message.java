// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.message;

import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.Text;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;

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
  public static final String DEBUG = "debug";
  public static final String INFO = "info";
  public static final String WARN = "warn";
  public static final String ERROR = "error";

  /**
   It is implied that choice types must equal idea types
   */
  public final static List<String> TYPES = ImmutableList.of(
    DEBUG,
    INFO,
    WARN,
    ERROR
  );
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

  public String getType() {
    return type;
  }


}
