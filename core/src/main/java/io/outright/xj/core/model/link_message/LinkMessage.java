// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link_message;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.LINK_MESSAGE;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class LinkMessage extends Message {
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "linkMessage";
  public static final String KEY_MANY = "linkMessages";

  @Override
  public void validate() throws BusinessException {
    if (this.linkId == null) {
      throw new BusinessException("Link ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (this.body == null || this.body.length() == 0) {
      throw new BusinessException("Body is required.");
    }
  }

  @Override
  public LinkMessage setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(LINK_MESSAGE.ID);
    linkId = record.get(LINK_MESSAGE.LINK_ID);
    type = record.get(LINK_MESSAGE.TYPE);
    body = record.get(LINK_MESSAGE.BODY);
    createdAt = record.get(LINK_MESSAGE.CREATED_AT);
    updatedAt = record.get(LINK_MESSAGE.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_MESSAGE.LINK_ID, linkId);
    fieldValues.put(LINK_MESSAGE.BODY, body);
    fieldValues.put(LINK_MESSAGE.TYPE, type);
    return fieldValues;
  }

  @Override
  public LinkMessage setBody(String body) {
    this.body = body;
    return this;
  }

  @Override
  public LinkMessage setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  @Override
  public LinkMessage setType(String type) {
    this.type = Text.LowerSlug(type);
    return this;
  }

}
