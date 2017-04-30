// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.PHASE;

public class Phase extends Entity {
  public final static String MAIN = "main";

  /**
   Name
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
   Idea
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
   Key
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
   Total
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
   Offset
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
   Density
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
   Tempo
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
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public void validate() throws BusinessException {
    if (this.name != null && this.name.length() == 0) {
      this.name = null;
    }
    if (this.ideaId == null) {
      throw new BusinessException("Idea ID is required.");
    }
    if (this.key != null && this.key.length() == 0) {
      this.key = null;
    }
    if (this.offset == null) {
      throw new BusinessException("Offset is required.");
    }
    if (this.density != null && this.density == 0) {
      this.density = null;
    }
    if (this.tempo != null && this.tempo == 0) {
      this.tempo = null;
    }
  }

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  @Override
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE.IDEA_ID, ideaId);
    fieldValues.put(PHASE.OFFSET, offset);
    fieldValues.put(PHASE.TOTAL, total != null ? total : DSL.val((String) null));
    fieldValues.put(PHASE.NAME, name != null ? name : DSL.val((String) null));
    fieldValues.put(PHASE.KEY, key != null ? key : DSL.val((String) null));
    fieldValues.put(PHASE.TEMPO, tempo != null ? tempo : DSL.val((String) null));
    fieldValues.put(PHASE.DENSITY, density != null ? density : DSL.val((String) null));
    return fieldValues;
  }

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "phase";
  public static final String KEY_MANY = "phases";

}
