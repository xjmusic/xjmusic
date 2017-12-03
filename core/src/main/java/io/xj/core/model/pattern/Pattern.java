// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PATTERN;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Pattern extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "pattern";
  public static final String KEY_MANY = "patterns";

  private String name;
  private String _type; // to hold value before validation
  private PatternType type;
  private ULong libraryId;
  private ULong userId;
  private String key;
  private Double density;
  private Double tempo;

  public String getName() {
    return name;
  }

  public Pattern setName(String value) {
    name = value;
    return this;
  }

  public PatternType getType() {
    return type;
  }

  public Pattern setType(String value) {
    _type = value;
    return this;
  }

  public ULong getLibraryId() {
    return libraryId;
  }

  public Pattern setLibraryId(BigInteger value) {
    libraryId = ULong.valueOf(value);
    return this;
  }

  public ULong getUserId() {
    return userId;
  }

  public Pattern setUserId(BigInteger value) {
    userId = ULong.valueOf(value);
    return this;
  }

  public String getKey() {
    return key;
  }

  public Pattern setKey(String value) {
    key = value;
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Pattern setDensity(Double value) {
    density = value;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Pattern setTempo(Double value) {
    tempo = value;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = PatternType.validate(_type);

    if (Objects.isNull(name) || name.isEmpty()) {
      throw new BusinessException("Name is required.");
    }
    if (Objects.isNull(libraryId)) {
      throw new BusinessException("Library ID is required.");
    }
    if (Objects.isNull(userId)) {
      throw new BusinessException("User ID is required.");
    }
    if (Objects.isNull(type)) {
      throw new BusinessException("Type is required.");
    }
    if (Objects.isNull(key) || key.isEmpty()) {
      throw new BusinessException("Key is required.");
    }
    if (Objects.isNull(density)) {
      throw new BusinessException("Density is required.");
    }
    if (Objects.isNull(tempo)) {
      throw new BusinessException("Tempo is required.");
    }
  }

  @Override
  public Pattern setFromRecord(Record record) throws BusinessException {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(PATTERN.ID);
    name = record.get(PATTERN.NAME);
    libraryId = record.get(PATTERN.LIBRARY_ID);
    userId = record.get(PATTERN.USER_ID);
    key = record.get(PATTERN.KEY);
    type = PatternType.validate(record.get(PATTERN.TYPE));
    tempo = record.get(PATTERN.TEMPO);
    density = record.get(PATTERN.DENSITY);
    createdAt = record.get(PATTERN.CREATED_AT);
    updatedAt = record.get(PATTERN.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PATTERN.NAME, name);
    fieldValues.put(PATTERN.LIBRARY_ID, libraryId);
    fieldValues.put(PATTERN.USER_ID, userId);
    fieldValues.put(PATTERN.KEY, key);
    fieldValues.put(PATTERN.TYPE, type);
    fieldValues.put(PATTERN.TEMPO, tempo);
    fieldValues.put(PATTERN.DENSITY, density);
    return fieldValues;
  }

}
