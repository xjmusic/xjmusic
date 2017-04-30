// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.instrument_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.INSTRUMENT_MEME;

public class InstrumentMeme extends Entity {

  // Instrument ID
  private BigInteger instrumentId;

  public ULong getInstrumentId() {
    return ULong.valueOf(instrumentId);
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
   Validate data.

   @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
    if (this.name == null) {
      throw new BusinessException("Name is required.");
    }
  }

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(INSTRUMENT_MEME.INSTRUMENT_ID, instrumentId);
    fieldValues.put(INSTRUMENT_MEME.NAME, name);
    return fieldValues;
  }

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "instrumentMeme";
  public static final String KEY_MANY = "instrumentMemes";


}
