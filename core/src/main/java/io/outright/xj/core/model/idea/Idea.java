// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.app.exception.BusinessException;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.IDEA;

public class Idea {
  public final static String MAIN = "main";

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
    this.type = type;
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
  void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.libraryId == null) {
      throw new BusinessException("Library ID is required.");
    }
    if (this.userId == null) {
      throw new BusinessException("User ID is required.");
    }
    // TODO: [core] Idea validate type is of acceptable idea-type
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
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
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(IDEA.NAME, name)
      .put(IDEA.LIBRARY_ID, libraryId)
      .put(IDEA.USER_ID, userId)
      .put(IDEA.KEY, key)
      .put(IDEA.TYPE, type)
      .put(IDEA.TEMPO, tempo)
      .put(IDEA.DENSITY, density)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "name:" + this.name +
      ", density:" + this.density +
      ", key:" + this.key +
      ", libraryId:" + this.libraryId +
      ", tempo:" + this.tempo +
      ", type:" + this.type +
      ", userId:" + this.userId +
    "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "idea";
  public static final String KEY_MANY = "ideas";

}
