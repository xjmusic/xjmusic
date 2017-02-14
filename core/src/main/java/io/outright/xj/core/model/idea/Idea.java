// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.Tables.IDEA;

public class Idea extends Entity {
  public final static String MACRO = "macro";
  public final static String MAIN = "main";
  public final static String RHYTHM = "rhythm";
  public final static String SUPPORT = "support";

  private final static List<String> allTypes = ImmutableList.of(
    MACRO,
    MAIN,
    RHYTHM,
    SUPPORT
  );

  // Name
  private String name;

  public String getName() {
    return name;
  }

  public Idea setName(String name) {
    this.name = name;
    return this;
  }

  // Type
  private String type;

  public String getType() {
    return type;
  }

  public Idea setType(String type) {
    this.type = Purify.LowerSlug(type);
    return this;
  }

  // Library
  private ULong libraryId;

  public ULong getLibraryId() {
    return libraryId;
  }

  public Idea setLibraryId(BigInteger libraryId) {
    this.libraryId = ULong.valueOf(libraryId);
    return this;
  }

  // User
  private ULong userId;

  public ULong getUserId() {
    return userId;
  }

  public Idea setUserId(BigInteger userId) {
    this.userId = ULong.valueOf(userId);
    return this;
  }

  // Key
  private String key;

  public String getKey() {
    return key;
  }

  public Idea setKey(String key) {
    this.key = key;
    return this;
  }

  // Density
  private Double density;

  public Double getDensity() {
    return density;
  }

  public Idea setDensity(Double density) {
    this.density = density;
    return this;
  }

  // Tempo
  private Double tempo;

  public Double getTempo() {
    return tempo;
  }

  public Idea setTempo(Double tempo) {
    this.tempo = tempo;
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
    if (this.libraryId == null) {
      throw new BusinessException("Library ID is required.");
    }
    if (this.userId == null) {
      throw new BusinessException("User ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!allTypes.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(allTypes) + ").");
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

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
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

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "idea";
  public static final String KEY_MANY = "ideas";

}
