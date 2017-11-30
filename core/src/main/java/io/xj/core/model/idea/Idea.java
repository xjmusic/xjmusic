// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.idea;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.IDEA;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Idea extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "idea";
  public static final String KEY_MANY = "ideas";

  private String name;
  private String _type; // to hold value before validation
  private IdeaType type;
  private ULong libraryId;
  private ULong userId;
  private String key;
  private Double density;
  private Double tempo;

  public String getName() {
    return name;
  }

  public Idea setName(String value) {
    name = value;
    return this;
  }

  public IdeaType getType() {
    return type;
  }

  public Idea setType(String value) {
    _type = value;
    return this;
  }

  public ULong getLibraryId() {
    return libraryId;
  }

  public Idea setLibraryId(BigInteger value) {
    libraryId = ULong.valueOf(value);
    return this;
  }

  public ULong getUserId() {
    return userId;
  }

  public Idea setUserId(BigInteger value) {
    userId = ULong.valueOf(value);
    return this;
  }

  public String getKey() {
    return key;
  }

  public Idea setKey(String value) {
    key = value;
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Idea setDensity(Double value) {
    density = value;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Idea setTempo(Double value) {
    tempo = value;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = IdeaType.validate(_type);

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
  public Idea setFromRecord(Record record) throws BusinessException {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(IDEA.ID);
    name = record.get(IDEA.NAME);
    libraryId = record.get(IDEA.LIBRARY_ID);
    userId = record.get(IDEA.USER_ID);
    key = record.get(IDEA.KEY);
    type = IdeaType.validate(record.get(IDEA.TYPE));
    tempo = record.get(IDEA.TEMPO);
    density = record.get(IDEA.DENSITY);
    createdAt = record.get(IDEA.CREATED_AT);
    updatedAt = record.get(IDEA.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(IDEA.NAME, name);
    fieldValues.put(IDEA.LIBRARY_ID, libraryId);
    fieldValues.put(IDEA.USER_ID, userId);
    fieldValues.put(IDEA.KEY, key);
    fieldValues.put(IDEA.TYPE, type);
    fieldValues.put(IDEA.TEMPO, tempo);
    fieldValues.put(IDEA.DENSITY, density);
    return fieldValues;
  }

}
