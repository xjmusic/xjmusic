// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.link;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.util.Value;

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

import static io.xj.core.Tables.LINK;
import static io.xj.core.app.config.Exposure.KEY_WAVEFORM_KEY;

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
  public static final String FILE_EXTENSION = "mp3";

  private ULong chainId;
  private String _state; // hold value before validation
  private LinkState state;
  private Timestamp beginAt;
  private String beginAtError;
  private Timestamp endAt; // optional
  private String endAtError;
  private String key;
  private UInteger total;
  private ULong offset;
  private Double density;
  private Double tempo;
  private String waveformKey;

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

  public LinkState getState() {
    return state;
  }

  public Link setState(String value) {
    _state = value;
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

  public String getWaveformKey() {
    return waveformKey;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    state = LinkState.validate(_state);

    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }

    if (this.beginAt == null) {
      throw new BusinessException("Begin-at is required." + (beginAtError != null ? " " + beginAtError : ""));
    }
    if (this.offset == null) {
      throw new BusinessException("Offset is required.");
    }
  }

  @Override
  public Link setFromRecord(Record record) throws BusinessException {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(LINK.ID);
    chainId = record.get(LINK.CHAIN_ID);
    offset = record.get(LINK.OFFSET);
    state = LinkState.validate(record.get(LINK.STATE));
    beginAt = record.get(LINK.BEGIN_AT);
    endAt = record.get(LINK.END_AT);
    total = record.get(LINK.TOTAL);
    density = record.get(LINK.DENSITY);
    key = record.get(LINK.KEY);
    tempo = record.get(LINK.TEMPO);
    waveformKey = record.get(LINK.WAVEFORM_KEY);
    createdAt = record.get(LINK.CREATED_AT);
    updatedAt = record.get(LINK.UPDATED_AT);
    return this;
  }

  /**
   Build a new Link model from a JSONObject representation

   @param json object
   @return Link model
   */
  public Link setFromJSON(JSONObject json) throws BusinessException {
    if (json.has(KEY_ID))
      id = ULong.valueOf(json.getBigInteger(KEY_ID));

    if (json.has(KEY_CHAIN_ID))
      chainId = ULong.valueOf(json.getBigInteger(KEY_CHAIN_ID));

    if (json.has(KEY_STATE))
      state = LinkState.validate(json.getString(KEY_STATE));

    if (json.has(KEY_OFFSET))
      offset = ULong.valueOf(json.getBigInteger(KEY_OFFSET));

    if (json.has(KEY_BEGIN_AT))
      beginAt = Timestamp.valueOf(json.get(KEY_BEGIN_AT).toString());

    if (json.has(KEY_END_AT))
      endAt = Timestamp.valueOf(json.get(KEY_END_AT).toString());

    if (json.has(KEY_DENSITY))
      density = json.getDouble(KEY_DENSITY);

    if (json.has(KEY_TEMPO))
      tempo = json.getDouble(KEY_TEMPO);

    if (json.has(KEY_TOTAL))
      total = UInteger.valueOf(json.getInt(KEY_TOTAL));

    if (json.has(KEY_KEY))
      key = json.getString(KEY_KEY);

    if (json.has(KEY_WAVEFORM_KEY))
      waveformKey = json.getString(KEY_WAVEFORM_KEY);

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
    // Excluding AUDIO.WAVEFORM_KEY a.k.a. waveformKey because that is read-only
    return fieldValues;
  }

  /**
   getContent offset of previous link

   @return previous link offset
   */
  public ULong getPreviousOffset() throws BusinessException {
    if (Objects.equals(offset, ULong.valueOf(0)))
      throw new BusinessException("Cannot getContent previous id of initial Link!");
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
    copy.waveformKey = waveformKey;
    copy.createdAt = createdAt;
    copy.updatedAt = updatedAt;
    return copy;
  }

}
