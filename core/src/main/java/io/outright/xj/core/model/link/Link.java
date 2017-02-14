// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import org.json.JSONObject;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK;

public class Link extends Entity {
  public static final String PLANNED = "planned";
  public static final String CRAFTING = "crafting";
  public static final String CRAFTED = "crafted";
  public static final String DUBBING = "dubbing";
  public static final String DUBBED = "dubbed";

  private final static List<String> allStates = ImmutableList.of(
    PLANNED,
    CRAFTING,
    CRAFTED,
    DUBBING,
    DUBBED
  );

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "link";
  public static final String KEY_MANY = "links";
  // attributes
  public static final String KEY_CHAIN_ID = "chainId";
  public static final String KEY_OFFSET = "offset";
  public static final String KEY_STATE = "state";
  public static final String KEY_BEGIN_AT = "beginAt";
  public static final String KEY_END_AT = "endAt";
  private static final String KEY_DENSITY = "density";
  private static final String KEY_TEMPO = "tempo";
  private static final String KEY_TOTAL = "total";
  private static final String KEY_KEY = "key";

  /**
   * ID
   */
  private ULong id;

  public ULong getId() {
    return id;
  }

  public Link setId(BigInteger id) {
    this.id = ULong.valueOf(id);
    return this;
  }

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

  /**
   * BeginAt
   */
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

  public Link setBeginAt(Timestamp beginAt) {
    this.beginAt = beginAt;
    return this;
  }

  /**
   * EndAt (optional)
   */
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

  public Link setEndAt(Timestamp endAt) {
    this.endAt = endAt;
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
  private ULong offset;

  public ULong getOffset() {
    return offset;
  }

  public Link setOffset(BigInteger offset) {
    if (offset != null) {
      this.offset = ULong.valueOf(offset);
    } else {
      this.offset = ULong.valueOf(0);
    }
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
    if (this.offset == null) {
      throw new BusinessException("Offset is required.");
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
    fieldValues.put(LINK.END_AT, endAt != null ? endAt : DSL.val((String) null));
    fieldValues.put(LINK.TOTAL, total != null ? total : DSL.val((String) null));
    fieldValues.put(LINK.DENSITY, density != null ? density : DSL.val((String) null));
    fieldValues.put(LINK.KEY, key != null ? key : DSL.val((String) null));
    fieldValues.put(LINK.TEMPO, tempo != null ? tempo : DSL.val((String) null));
    return fieldValues;
  }

  /**
   * Build a new Link model from a JSONObject representation
   * @param json object
   * @return Link model
   */
  public static Link fromJSON(JSONObject json) {
    Link link = new Link();
    if (json.has(KEY_ID)) {
      link.setId(json.getBigInteger(KEY_ID));
    }
    if (json.has(KEY_CHAIN_ID)) {
      link.setChainId(json.getBigInteger(KEY_CHAIN_ID));
    }
    if (json.has(KEY_STATE)) {
      link.setState(json.getString(KEY_STATE));
    }
    if (json.has(KEY_OFFSET)) {
      link.setOffset(json.getBigInteger(KEY_OFFSET));
    }
    if (json.has(KEY_BEGIN_AT)) {
      link.setBeginAt(json.get(KEY_BEGIN_AT).toString());
    }
    if (json.has(KEY_END_AT)) {
      link.setEndAt(json.get(KEY_END_AT).toString());
    }
    if (json.has(KEY_DENSITY)) {
      link.setDensity(json.getDouble(KEY_DENSITY));
    }
    if (json.has(KEY_TEMPO)) {
      link.setTempo(json.getDouble(KEY_TEMPO));
    }
    if (json.has(KEY_TOTAL)) {
      link.setTotal(json.getInt(KEY_TOTAL));
    }
    if (json.has(KEY_KEY)) {
      link.setKey(json.getString(KEY_KEY));
    }
    return link;
  }
}
