// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.util.Text;
import io.outright.xj.core.util.Value;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import org.json.JSONObject;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.LINK;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Link extends Entity {
  public static final String PLANNED = "planned";
  public static final String CRAFTING = "crafting";
  public static final String CRAFTED = "crafted";
  public static final String DUBBING = "dubbing";
  public static final String DUBBED = "dubbed";
  public static final String FAILED = "failed";
  // list of all states
  public final static List<String> STATES = ImmutableList.of(
    PLANNED,
    CRAFTING,
    CRAFTED,
    DUBBING,
    DUBBED,
    FAILED
  );

  /**
   For use in maps.
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
   Chain
   */
  private ULong chainId;
  /**
   State
   */
  private String state;
  /**
   BeginAt
   */
  private Timestamp beginAt;
  private String beginAtError;
  /**
   EndAt (optional)
   */
  private Timestamp endAt;
  private String endAtError;
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

  /**
   Build a new Link model from a JSONObject representation

   @param json object
   @return Link model
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

  /**
   Validate a state

   @param state to validate
   @throws BusinessException if invalid
   */
  public static void validateState(String state) throws BusinessException {
    if (state == null || state.length() == 0) {
      throw new BusinessException("State is required.");
    }
    if (!STATES.contains(state)) {
      throw new BusinessException("'" + state + "' is not a valid state (" + CSV.join(STATES) + ").");
    }
  }

  /**
   Whether this Link is at offset 0

   @return true if offset is 0
   */
  public boolean isInitial() {
    return getOffset().equals(ULong.valueOf(0));
  }

  public ULong getChainId() {
    return chainId;
  }

  public Link setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  public String getState() {
    return state;
  }

  public Link setState(String state) {
    this.state = Text.LowerSlug(state);
    return this;
  }

  public Timestamp getBeginAt() {
    return beginAt;
  }

  public Link setBeginAtTimestamp(Timestamp beginAt) {
    this.beginAt = beginAt;
    return this;
  }

  public Link setBeginAt(String beginAt) {
    try {
      this.beginAt = buildTimestampOf(beginAt);
    } catch (Exception e) {
      beginAtError = e.getMessage();
    }
    return this;
  }

  public Timestamp getEndAt() {
    return endAt;
  }

  public Link setEndAtTimestamp(Timestamp endAt) {
    this.endAt = endAt;
    return this;
  }

  public Link setEndAt(String endAt) {
    try {
      this.endAt = buildTimestampOf(endAt);
    } catch (Exception e) {
      endAtError = e.getMessage();
    }
    return this;
  }

  public String getKey() {
    return key;
  }

  public Link setKey(String key) {
    this.key = key;
    return this;
  }

  public UInteger getTotal() {
    return total;
  }

  public Link setTotal(Integer total) {
    this.total = UInteger.valueOf(total);
    return this;
  }

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

  public Double getDensity() {
    return density;
  }

  public Link setDensity(Double density) {
    this.density = density;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Link setTempo(double tempo) {
    this.tempo = tempo;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    validateState(this.state);
    if (this.beginAt == null) {
      throw new BusinessException("Begin-at is required." + (beginAtError != null ? " " + beginAtError : ""));
    }
    if (this.offset == null) {
      throw new BusinessException("Offset is required.");
    }
  }

  @Override
  public Link setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(LINK.ID);
    chainId = record.get(LINK.CHAIN_ID);
    offset = record.get(LINK.OFFSET);
    state = record.get(LINK.STATE);
    beginAt = record.get(LINK.BEGIN_AT);
    endAt = record.get(LINK.END_AT);
    total = record.get(LINK.TOTAL);
    density = record.get(LINK.DENSITY);
    key = record.get(LINK.KEY);
    tempo = record.get(LINK.TEMPO);
    createdAt = record.get(LINK.CREATED_AT);
    updatedAt = record.get(LINK.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
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
   get offset of previous link

   @return previous link offset
   */
  public ULong getPreviousOffset() throws BusinessException {
    if (Objects.equals(offset, ULong.valueOf(0)))
      throw new BusinessException("Cannot get previous id of initial Link!");
    return Value.inc(offset, -1);
  }

  /**
   Copy the link to a new one (without id)
   @return copied Link
   */
  public Link copy() {
    Link copy = new Link();
    copy.chainId = chainId;
    copy.offset = offset;
    copy.state = state;
    copy.beginAt = beginAt;
    copy.endAt = endAt;
    copy.total = total;
    copy.density = density;
    copy.key = key;
    copy.tempo = tempo;
    copy.createdAt = createdAt;
    copy.updatedAt = updatedAt;
    return copy;
  }
}
