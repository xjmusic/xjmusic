// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.instrument_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.util.Purify;

import com.google.common.collect.ImmutableMap;
import org.jooq.Field;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.INSTRUMENT_MEME;

public class InstrumentMeme {

  // Instrument ID
  private BigInteger instrumentId;
  public BigInteger getInstrumentId() {
    return instrumentId;
  }
  public InstrumentMeme setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  // Name
  private String name;
  public String getName() {
    return name;
  }
  public InstrumentMeme setName(String name) {
    this.name = Purify.ProperSlug(name);
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  void validate() throws BusinessException {
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
    if (this.name == null) {
      throw new BusinessException("Name is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    return new ImmutableMap.Builder<Field, Object>()
      .put(INSTRUMENT_MEME.INSTRUMENT_ID, instrumentId)
      .put(INSTRUMENT_MEME.NAME, name)
      .build();
  }

  @Override
  public String toString() {
    return "{" +
      "instrumentId:" + this.instrumentId +
      "name:" + this.name +
      "}";
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "instrumentMeme";
  public static final String KEY_MANY = "instrumentMemes";


}
