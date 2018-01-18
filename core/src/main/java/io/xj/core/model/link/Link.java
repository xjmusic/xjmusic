// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.timestamp.TimestampUTC;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Objects;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Link extends Entity {
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "link";
  public static final String KEY_MANY = "links";
  // attributes
  public static final String FILE_EXTENSION = "mp3";

  private BigInteger chainId;
  private String _state; // hold value before validation
  private LinkState state;
  private Timestamp beginAt;
  private String beginAtError;
  private Timestamp endAt; // optional
  private String endAtError;
  private String key;
  private Integer total;
  private BigInteger offset;
  private Double density;
  private Double tempo;
  private String waveformKey;

  public Link() {
  }

  public Link(long id) {
    this.id = BigInteger.valueOf(id);
  }

  /**
   Whether this Link is at offset 0

   @return true if offset is 0
   */
  public boolean isInitial() {
    return getOffset().equals(BigInteger.valueOf(0));
  }

  public BigInteger getChainId() {
    return chainId;
  }

  public Link setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  public LinkState getState() {
    return state;
  }

  public Link setState(String value) {
    _state = value;
    return this;
  }

  public Link setStateEnum(LinkState value) {
    state = value;
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
      this.beginAt = TimestampUTC.valueOf(beginAt);
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
      this.endAt = TimestampUTC.valueOf(endAt);
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

  public Integer getTotal() {
    return total;
  }

  public Link setTotal(Integer total) {
    this.total = total;
    return this;
  }

  public BigInteger getOffset() {
    return offset;
  }

  public Link setOffset(BigInteger offset) {
    if (Objects.nonNull(offset)) {
      this.offset = offset;
    } else {
      this.offset = BigInteger.valueOf(0);
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

  public Link setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  public String getWaveformKey() {
    return waveformKey;
  }

  public Link setWaveformKey(String waveformKey) {
    this.waveformKey = waveformKey;
    return this;
  }


  @Override
  public BigInteger getParentId() {
    return chainId;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    if (Objects.isNull(state)) {
      state = LinkState.validate(_state);
    }

    if (Objects.isNull(chainId)) {
      throw new BusinessException("Chain ID is required.");
    }

    if (Objects.isNull(beginAt)) {
      throw new BusinessException("Begin-at is required." + (Objects.nonNull(beginAtError) ? " " + beginAtError : ""));
    }

    if (Objects.nonNull(endAtError) && !endAtError.isEmpty()) {
      throw new BusinessException("End-at must be isValid time." + endAtError);
    }

    if (Objects.isNull(offset)) {
      throw new BusinessException("Offset is required.");
    }
  }

  /**
   get offset of previous link

   @return previous link offset
   */
  BigInteger getPreviousOffset() throws BusinessException {
    if (Objects.equals(offset, BigInteger.valueOf(0)))
      throw new BusinessException("Cannot get previous id of initial Link!");
    return Value.inc(offset, -1);
  }

  /**
   Override state

   @param state new value
   */
  public void overrideState(LinkState state) {
    this.state = state;
  }
}
