// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.IDEA;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Idea extends Entity {
  public final static String MACRO = "macro";
  public final static String MAIN = "main";
  public final static String RHYTHM = "rhythm";
  public final static String SUPPORT = "support";
  // list of all types
  public final static List<String> TYPES = ImmutableList.of(
    MACRO,
    MAIN,
    RHYTHM,
    SUPPORT
  );
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "idea";
  public static final String KEY_MANY = "ideas";
  // Name
  private String name;
  // Type
  private String type;
  // Library
  private ULong libraryId;
  // User
  private ULong userId;
  // Key
  private String key;
  // Density
  private Double density;
  // Tempo
  private Double tempo;

  public String getName() {
    return name;
  }

  public Idea setName(String name) {
    this.name = name;
    return this;
  }

  public String getType() {
    return type;
  }

  public Idea setType(String type) {
    this.type = Text.LowerSlug(type);
    return this;
  }

  public ULong getLibraryId() {
    return libraryId;
  }

  public Idea setLibraryId(BigInteger libraryId) {
    this.libraryId = ULong.valueOf(libraryId);
    return this;
  }

  public ULong getUserId() {
    return userId;
  }

  public Idea setUserId(BigInteger userId) {
    this.userId = ULong.valueOf(userId);
    return this;
  }

  public String getKey() {
    return key;
  }

  public Idea setKey(String key) {
    this.key = key;
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Idea setDensity(Double density) {
    this.density = density;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Idea setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.libraryId == null) {
      throw new BusinessException("Library ID is required.");
    }
    if (this.userId == null) {
      throw new BusinessException("User ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (this.key == null || this.key.length() == 0) {
      throw new BusinessException("Key is required.");
    }
    if (this.density == null) {
      throw new BusinessException("Density is required.");
    }
    if (this.tempo == null) {
      throw new BusinessException("Tempo is required.");
    }
  }

  @Override
  public Idea setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(IDEA.ID);
    name = record.get(IDEA.NAME);
    libraryId = record.get(IDEA.LIBRARY_ID);
    userId = record.get(IDEA.USER_ID);
    key = record.get(IDEA.KEY);
    type = record.get(IDEA.TYPE);
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
