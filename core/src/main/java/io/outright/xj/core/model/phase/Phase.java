// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import com.google.common.collect.ImmutableMap;
import org.jooq.Field;
import org.jooq.types.ULong;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.PHASE;

public class Phase extends Entity {
  public final static String MAIN = "main";

  /**
   * Name
   */
  private String name;

  public String getName() {
    return name;
  }

  public Phase setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Idea
   */
  private ULong ideaId;

  public ULong getIdeaId() {
    return ideaId;
  }

  public Phase setIdeaId(BigInteger ideaId) {
    this.ideaId = ULong.valueOf(ideaId);
    return this;
  }

  /**
   * Key
   */
  private String key;

  public String getKey() {
    return key;
  }

  public Phase setKey(String key) {
    this.key = key;
    return this;
  }

  /**
   * Total
   */
  private Integer total;

  public Integer getTotal() {
    return total;
  }

  public Phase setTotal(Integer total) {
    this.total = total;
    return this;
  }

  /**
   * Offset
   */
  private Integer offset;

  public Integer getOffset() {
    return offset;
  }

  public Phase setOffset(Integer offset) {
    this.offset = offset;
    return this;
  }

  /**
   * Density
   */
  private Double density;

  public Double getDensity() {
    return density;
  }

  public Phase setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   * Tempo
   */
  private Double tempo;

  public Double getTempo() {
    return tempo;
  }

  public Phase setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  @Override
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.ideaId == null) {
      throw new BusinessException("Idea ID is required.");
    }
    if (this.key == null || this.key.length() == 0) {
      throw new BusinessException("Key is required.");
    }
    if (this.total == null) {
      throw new BusinessException("Total is required.");
    }
    if (this.offset == null) {
      throw new BusinessException("Offset is required.");
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
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(PHASE.NAME, name)
      .put(PHASE.IDEA_ID, ideaId)
      .put(PHASE.KEY, key)
      .put(PHASE.TOTAL, total)
      .put(PHASE.OFFSET, offset)
      .put(PHASE.TEMPO, tempo)
      .put(PHASE.DENSITY, density)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "name:" + this.name +
      ", ideaId:" + this.ideaId +
      ", key:" + this.key +
      ", total:" + this.total +
      ", offset:" + this.offset +
      ", density:" + this.density +
      ", tempo:" + this.tempo +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "phase";
  public static final String KEY_MANY = "phases";

}
