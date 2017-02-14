// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.link_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK_CHORD;

public class LinkChord extends Entity {

  /**
   * Name
   */
  private String name;

  public String getName() {
    return name;
  }

  public LinkChord setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Link
   */
  private ULong linkId;

  public ULong getLinkId() {
    return linkId;
  }

  public LinkChord setLinkId(BigInteger linkId) {
    this.linkId = ULong.valueOf(linkId);
    return this;
  }

  /**
   * Position
   */
  private Double position;

  public Double getPosition() {
    return position;
  }

  public LinkChord setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
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

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_CHORD.NAME, name);
    fieldValues.put(LINK_CHORD.LINK_ID, linkId);
    fieldValues.put(LINK_CHORD.POSITION, position);
    return fieldValues;
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "linkChord";
  public static final String KEY_MANY = "linkChords";

}
