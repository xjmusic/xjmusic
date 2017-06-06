// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.link_chord;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.ChordEntity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.LINK_CHORD;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class LinkChord extends ChordEntity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "linkChord";
  public static final String KEY_MANY = "linkChords";
  /**
   Link
   */
  private ULong linkId;


  public LinkChord setName(String name) {
    this.name = name;
    return this;
  }

  public ULong getLinkId() {
    return linkId;
  }

  public LinkChord setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  public LinkChord setPosition(Double position) {
    this.position = position;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.linkId == null) {
      throw new BusinessException("Link ID is required.");
    }
    super.validate();
  }

  @Override
  public LinkChord setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(LINK_CHORD.ID);
    name = record.get(LINK_CHORD.NAME);
    linkId = record.get(LINK_CHORD.LINK_ID);
    position = record.get(LINK_CHORD.POSITION);
    createdAt = record.get(LINK_CHORD.CREATED_AT);
    updatedAt = record.get(LINK_CHORD.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_CHORD.NAME, name);
    fieldValues.put(LINK_CHORD.LINK_ID, linkId);
    fieldValues.put(LINK_CHORD.POSITION, position);
    return fieldValues;
  }

}
