// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.phase;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PHASE;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Phase extends Entity {
  public final static String MAIN = "main";
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "phase";
  public static final String KEY_MANY = "phases";
  /**
   Name
   */
  private String name;
  /**
   Pattern
   */
  private ULong patternId;
  /**
   Key
   */
  private String key;
  /**
   Total
   */
  private UInteger total;
  /**
   Offset
   */
  private ULong offset;
  /**
   Density
   */
  private Double density;
  /**
   Tempo
   */
  private Double tempo;

  public String getName() {
    return name;
  }

  public Phase setName(String name) {
    this.name = name;
    return this;
  }

  public ULong getPatternId() {
    return patternId;
  }

  public Phase setPatternId(BigInteger patternId) {
    this.patternId = ULong.valueOf(patternId);
    return this;
  }

  public String getKey() {
    return key;
  }

  public Phase setKey(String key) {
    this.key = key;
    return this;
  }

  public UInteger getTotal() {
    return total;
  }

  public Phase setTotal(Integer total) {
    this.total = UInteger.valueOf(total);
    return this;
  }

  public ULong getOffset() {
    return offset;
  }

  public Phase setOffset(BigInteger offset) {
    this.offset = ULong.valueOf(offset);
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Phase setDensity(Double density) {
    this.density = density;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Phase setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.name != null && this.name.length() == 0) {
      this.name = null;
    }
    if (this.patternId == null) {
      throw new BusinessException("Pattern ID is required.");
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

  @Override
  public Phase setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(PHASE.ID);
    patternId = record.get(PHASE.PATTERN_ID);
    offset = record.get(PHASE.OFFSET);
    total = record.get(PHASE.TOTAL);
    name = record.get(PHASE.NAME);
    key = record.get(PHASE.KEY);
    tempo = record.get(PHASE.TEMPO);
    density = record.get(PHASE.DENSITY);
    createdAt = record.get(PHASE.CREATED_AT);
    updatedAt = record.get(PHASE.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE.PATTERN_ID, patternId);
    fieldValues.put(PHASE.OFFSET, offset);
    fieldValues.put(PHASE.TOTAL, total != null ? total : DSL.val((String) null));
    fieldValues.put(PHASE.NAME, name != null ? name : DSL.val((String) null));
    fieldValues.put(PHASE.KEY, key != null ? key : DSL.val((String) null));
    fieldValues.put(PHASE.TEMPO, tempo != null ? tempo : DSL.val((String) null));
    fieldValues.put(PHASE.DENSITY, density != null ? density : DSL.val((String) null));
    return fieldValues;
  }

}
