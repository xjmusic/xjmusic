// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.CSV.CSV;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK;

public class Link extends Entity {
  public static final String PLANNED = "planned";
  public static final String CHOOSING = "choosing";
  public static final String CHOSEN = "chosen";
  public static final String MIXING = "mixing";
  public static final String MIXED = "mixed";

  private final static List<String> allStates = ImmutableList.of(
    PLANNED,
    CHOOSING,
    CHOSEN,
    MIXING,
    MIXED
  );


  /**
   * Chain
   */
  private ULong chainId;

  public ULong getChainId() {
    return chainId;
  }

  public Link setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  /**
   * State
   */
  private String state;

  public String getState() {
    return state;
  }

  public Link setState(String state) {
    this.state = Purify.LowerSlug(state);
    return this;
  }

  // BeginAt
  private Timestamp beginAt;
  private String beginAtError;

  public Timestamp getBeginAt() {
    return beginAt;
  }

  public Link setBeginAt(String beginAt) {
    try {
      this.beginAt = Timestamp.valueOf(beginAt);
    } catch (Exception e) {
      beginAtError = e.getMessage();
    }
    return this;
  }

  // EndAt
  private Timestamp endAt;
  private String endAtError;

  public Timestamp getEndAt() {
    return endAt;
  }

  public Link setEndAt(String endAt) {
    try {
      this.endAt = Timestamp.valueOf(endAt);
    } catch (Exception e) {
      endAtError = e.getMessage();
    }
    return this;
  }

  /**
   * Key
   */
  private String key;

  public String getKey() {
    return key;
  }

  public Link setKey(String key) {
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

  public Link setTotal(Integer total) {
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

  public Link setOffset(Integer offset) {
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

  public Link setDensity(Double density) {
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

  public Link setTempo(Double tempo) {
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
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.state == null || this.state.length() == 0) {
      throw new BusinessException("State is required.");
    }
    if (!allStates.contains(this.state)) {
      throw new BusinessException("'" + this.state + "' is not a valid state (" + CSV.join(allStates) + ").");
    }
    if (this.beginAt == null) {
      throw new BusinessException("Begin-at is required." + (beginAtError != null ? " " + beginAtError : ""));
    }
    if (this.endAt == null) {
      throw new BusinessException("End-at is required." + (endAtError != null ? " " + endAtError : ""));
    }
    if (this.offset == null) {
      throw new BusinessException("Offset is required.");
    }
    if (this.total == null) {
      throw new BusinessException("Total is required.");
    }
    if (this.density == null || this.density == 0) {
      throw new BusinessException("Density is required.");
    }
    if (this.key == null || this.key.length() == 0) {
      throw new BusinessException("Key is required.");
    }
    if (this.tempo == null || this.tempo == 0) {
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
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK.CHAIN_ID, chainId);
    fieldValues.put(LINK.OFFSET, offset);
    fieldValues.put(LINK.STATE, state);
    fieldValues.put(LINK.BEGIN_AT, beginAt);
    fieldValues.put(LINK.END_AT, endAt);
    fieldValues.put(LINK.TOTAL, total);
    fieldValues.put(LINK.DENSITY, density != null ? density : DSL.val((String) null));
    fieldValues.put(LINK.KEY, key != null ? key : DSL.val((String) null));
    fieldValues.put(LINK.TEMPO, tempo != null ? tempo : DSL.val((String) null));
    return fieldValues;
  }

  @Override
  public String toString() {
    return "{" +
      "offset:" + this.offset +
      ", chainId:" + this.chainId +
      ", key:" + this.key +
      ", total:" + this.total +
      ", density:" + this.density +
      ", tempo:" + this.tempo +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "link";
  public static final String KEY_MANY = "links";

}
