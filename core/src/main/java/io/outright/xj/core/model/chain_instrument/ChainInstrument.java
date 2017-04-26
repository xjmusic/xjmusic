// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_instrument;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN_INSTRUMENT;

public class ChainInstrument extends Entity {

  // Chain ID
  private BigInteger chainId;

  public ULong getChainId() {
    return ULong.valueOf(chainId);
  }

  public ChainInstrument setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  // Instrument ID
  private BigInteger instrumentId;

  public ULong getInstrumentId() {
    return ULong.valueOf(instrumentId);
  }

  public ChainInstrument setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_INSTRUMENT.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_INSTRUMENT.INSTRUMENT_ID, instrumentId);
    return fieldValues;
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "chainInstrument";
  public static final String KEY_MANY = "chainInstruments";


}
