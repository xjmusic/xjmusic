// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.LINK_CHORD;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class LinkChord extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "linkChord";
  public static final String KEY_MANY = "linkChords";
  /**
   Name
   */
  private String name;
  /**
   Link
   */
  private ULong linkId;
  /**
   Position
   */
  private Double position;

  public String getName() {
    return name;
  }

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

  public Double getPosition() {
    return position;
  }

  public LinkChord setPosition(Double position) {
    this.position = position;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.linkId == null) {
      throw new BusinessException("Link ID is required.");
    }
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
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
